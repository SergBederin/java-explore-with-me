FROM amazoncorretto:11
COPY /server/target/*.jar stat_svc.jar
ENTRYPOINT ["java","-jar","/stat_svc.jar"]