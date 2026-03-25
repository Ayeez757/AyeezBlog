package cn.ayeez.blogserver.service.postServer.Impl;

import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogpojo.dto.request.LogVersionBody;
import cn.ayeez.blogpojo.dto.response.LogVersionDetail;
import cn.ayeez.blogpojo.entity.BlogLogEntry;
import cn.ayeez.blogpojo.entity.BlogLogVersion;
import cn.ayeez.blogserver.mapper.BlogLogEntryMapper;
import cn.ayeez.blogserver.mapper.BlogLogVersionMapper;
import cn.ayeez.blogserver.service.postServer.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LogServiceImpl implements LogService {

    @Autowired
    private BlogLogVersionMapper blogLogVersionMapper;

    @Autowired
    private BlogLogEntryMapper blogLogEntryMapper;

    @Override
    public List<LogVersionDetail> list(String keyword) {
        List<BlogLogVersion> versions = blogLogVersionMapper.listAll(keyword);
        if (versions == null || versions.isEmpty()) {
            return new ArrayList<>();
        }

        List<LogVersionDetail> result = new ArrayList<>();
        for (BlogLogVersion version : versions) {
            List<BlogLogEntry> entries = blogLogEntryMapper.listEntriesByVersionId(version.getId());
            List<String> changes = entries == null
                    ? Collections.emptyList()
                    : entries.stream()
                    .map(BlogLogEntry::getContent)
                    .filter(Objects::nonNull)
                    .filter(s -> !s.trim().isEmpty())
                    .collect(Collectors.toList());

            LogVersionDetail detail = new LogVersionDetail();
            detail.setId(version.getId());
            detail.setVersion(version.getVersion());
            detail.setDate(version.getLogDate());
            detail.setChanges(changes);
            detail.setCurrent(version.getIsCurrent() != null && version.getIsCurrent() == 1);

            result.add(detail);
        }
        return result;
    }

    @Override
    public LogVersionDetail current() {
        BlogLogVersion currentVersion = blogLogVersionMapper.selectCurrent();
        if (currentVersion == null) {
            return null;
        }
        List<BlogLogEntry> entries = blogLogEntryMapper.listEntriesByVersionId(currentVersion.getId());
        List<String> changes = entries == null
                ? Collections.emptyList()
                : entries.stream()
                .map(BlogLogEntry::getContent)
                .filter(Objects::nonNull)
                .filter(s -> !s.trim().isEmpty())
                .collect(Collectors.toList());

        LogVersionDetail detail = new LogVersionDetail();
        detail.setId(currentVersion.getId());
        detail.setVersion(currentVersion.getVersion());
        detail.setDate(currentVersion.getLogDate());
        detail.setChanges(changes);
        detail.setCurrent(true);
        return detail;
    }

    @Override
    @Transactional
    public Result add(LogVersionBody body) {
        Result validate = validateBody(body, false);
        if (validate != null) {
            return validate;
        }

        String versionStr = body.getVersion().trim();
        List<String> changes = normalizeChanges(body.getChanges());

        BlogLogVersion version = new BlogLogVersion();
        version.setVersion(versionStr);
        version.setLogDate(body.getDate());
        version.setIsCurrent(0);
        blogLogVersionMapper.insert(version);

        if (version.getId() != null) {
            blogLogEntryMapper.insertEntries(version.getId(), changes);
        }

        return Result.success();
    }

    @Override
    @Transactional
    public Result update(LogVersionBody body) {
        Result validate = validateBody(body, true);
        if (validate != null) {
            return validate;
        }

        String versionStr = body.getVersion().trim();
        List<String> changes = normalizeChanges(body.getChanges());

        BlogLogVersion update = new BlogLogVersion();
        update.setId(body.getId());
        update.setVersion(versionStr);
        update.setLogDate(body.getDate());
        blogLogVersionMapper.update(update);

        blogLogEntryMapper.deleteByVersionId(body.getId());
        blogLogEntryMapper.insertEntries(body.getId(), changes);
        return Result.success();
    }

    @Override
    @Transactional
    public Result delete(Long id) {
        if (id == null) {
            return Result.error(400, "日志版本ID不能为空");
        }

        blogLogEntryMapper.deleteByVersionId(id);
        blogLogVersionMapper.deleteById(id);

        BlogLogVersion current = blogLogVersionMapper.selectCurrent();
        if (current == null) {
            BlogLogVersion latest = blogLogVersionMapper.selectLatest();
            if (latest != null) {
                blogLogVersionMapper.resetCurrent();
                blogLogVersionMapper.setCurrentById(latest.getId());
            }
        }

        return Result.success();
    }

    @Override
    @Transactional
    public Result setCurrent(Long id) {
        if (id == null) {
            return Result.error(400, "日志版本ID不能为空");
        }
        BlogLogVersion exists = blogLogVersionMapper.selectById(id);
        if (exists == null) {
            return Result.error(404, "日志版本不存在");
        }

        blogLogVersionMapper.resetCurrent();
        blogLogVersionMapper.setCurrentById(id);
        return Result.success();
    }

    private Result validateBody(LogVersionBody body, boolean requireId) {
        if (body == null) {
            return Result.error(400, "参数不能为空");
        }
        if (requireId && body.getId() == null) {
            return Result.error(400, "日志版本ID不能为空");
        }
        if (body.getVersion() == null || body.getVersion().trim().isEmpty()) {
            return Result.error(400, "版本号不能为空");
        }
        if (body.getDate() == null) {
            return Result.error(400, "版本日期不能为空");
        }
        if (body.getChanges() == null || body.getChanges().isEmpty()) {
            return Result.error(400, "变更内容不能为空");
        }
        return null;
    }

    private List<String> normalizeChanges(List<String> changes) {
        if (changes == null) {
            return Collections.emptyList();
        }
        return changes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}

