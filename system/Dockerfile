FROM clojure:tools-deps AS build
WORKDIR /usr/src/app
COPY deps.edn ./
RUN --mount=type=cache,target=/root/.m2/repository \
    --mount=type=cache,target=/root/.gitlibs \
    clojure -P
COPY . ./
RUN clj -T:build uber


FROM eclipse-temurin:21-jre
COPY --from=build /usr/src/app/target/*.jar ./
RUN mkdir ~/.datomic && echo '{:storage-dir :mem}' > ~/.datomic/local.edn
EXPOSE 3000
CMD ["java", "-jar", "communitycal-server-standalone.jar", ":port", "3000"]
