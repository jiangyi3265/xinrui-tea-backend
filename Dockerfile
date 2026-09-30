FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /source
COPY . .
RUN mvn -B -pl ruoyi-admin -am package && mkdir /runtime && cd /runtime && jar -xf /source/ruoyi-admin/target/ruoyi-admin.jar

FROM eclipse-temurin:17-jre-jammy
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/* \
    && groupadd --gid 10001 tea && useradd --uid 10001 --gid tea --no-create-home tea \
    && mkdir -p /data/uploads /data/logs && chown -R tea:tea /data
WORKDIR /app
COPY --from=build /runtime/BOOT-INF/classes ./classes
COPY --from=build /runtime/BOOT-INF/lib ./lib
COPY sql ./sql
USER tea
ENV TEA_BOOTSTRAP_DIR=/app/sql TEA_UPLOAD_DIR=/data/uploads TEA_LOG_DIR=/data/logs
EXPOSE 8080
CMD ["java","-cp","/app/classes:/app/lib/*","com.ruoyi.RuoYiApplication","--spring.profiles.active=druid,tea","--spring.datasource.druid.statViewServlet.enabled=false","--swagger.enabled=false"]
