FROM clojure:tools-deps
WORKDIR /usr/src/app
COPY deps.edn ./
RUN clojure -P
COPY . ./

EXPOSE 3000
CMD ["clojure", "-X", "communitycal.web.server/-main"]
