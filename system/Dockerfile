FROM clojure:tools-deps
WORKDIR /usr/src/app
COPY deps.edn ./
RUN clojure -P
RUN mkdir ~/.datomic && echo '{:storage-dir :mem}' > ~/.datomic/local.edn
COPY . ./

EXPOSE 3000
CMD ["clojure", "-X", "communitycal.web.server/-main"]
