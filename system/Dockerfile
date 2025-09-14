FROM clojure:tools-deps AS build
WORKDIR /usr/src/app
COPY deps.edn ./
RUN --mount=type=cache,target=/root/.m2/repository \
    --mount=type=cache,target=/root/.gitlibs \
    clojure -P
COPY . ./

# This shouldn’t be needed just to compile and create the uberjar, but for some reason it is.
RUN mkdir ~/.datomic && echo '{:storage-dir :mem}' > ~/.datomic/local.edn

RUN --mount=type=cache,target=/root/.m2/repository \
    --mount=type=cache,target=/root/.gitlibs \
    clj -T:build uber


FROM eclipse-temurin:21-jre AS deploy
WORKDIR /usr/server
COPY --from=build /usr/src/app/target/*.jar  ./
COPY --from=build /usr/src/app/resources resources
RUN mkdir ~/.datomic && echo '{:storage-dir :mem}' > ~/.datomic/local.edn
EXPOSE 3000
CMD ["java", "-jar", "communitycal-server-standalone.jar", ":port", "3000"]
