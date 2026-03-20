# 1. 基础镜像：直接使用 JRE 21 (Ubuntu 22.04)
FROM eclipse-temurin:21-jre-jammy

# 2. 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 3. 创建工作目录
WORKDIR /app

# 4. 拷贝 jar 包
COPY  blog-server-0.0.1-SNAPSHOT.jar app.jar

# 5. 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]