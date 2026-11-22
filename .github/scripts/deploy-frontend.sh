#!/usr/bin/env bash
set -euo pipefail

: "${SERVER_HOST:?}" "${SERVER_USER:?}" "${SERVER_PORT:?}" "${SERVER_SSH_KEY:?}"
dist=AyeezBlog-Frontend/dist
target="$SERVER_USER@$SERVER_HOST:/var/lib/docker/volumes/html/_data/"
test -s "$dist/index.html"

ssh_dir=$(mktemp -d)
trap 'rm -f -- "$ssh_dir/key" "$ssh_dir/known_hosts"; rmdir -- "$ssh_dir"' EXIT
printf '%s\n' "$SERVER_SSH_KEY" | tr -d '\r' > "$ssh_dir/key"
chmod 600 "$ssh_dir/key"
unset SERVER_SSH_KEY
export RSYNC_RSH="ssh -i '$ssh_dir/key' -p $SERVER_PORT -o BatchMode=yes -o IdentitiesOnly=yes -o StrictHostKeyChecking=accept-new -o UserKnownHostsFile='$ssh_dir/known_hosts' -o ConnectTimeout=15 -o ServerAliveInterval=15 -o ServerAliveCountMax=3"

sync_with_retry() {
  local attempt status
  for attempt in 1 2 3; do
    if rsync -az --timeout=90 "$@" "$target"; then
      return 0
    else
      status=$?
    fi

    case "$status" in
      10|12|30|255)
        if [ "$attempt" -eq 3 ]; then
          return "$status"
        fi
        echo "rsync exited with $status on attempt $attempt. Retrying." >&2
        sleep "$((attempt * 5))"
        ;;
      *) return "$status" ;;
    esac
  done
}

# Keep hashed assets referenced by existing pages and switch index.html only after the upload succeeds.
sync_with_retry --exclude=/admin/ --exclude=/index.html "$dist/"
sync_with_retry --checksum "$dist/index.html"
