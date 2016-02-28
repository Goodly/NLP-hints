
# Text Thresher Hints

#### Running Annotator.java.

```
mvn compile
mvn -q exec:java -Dexec.mainClass="annotator.Annotator" > out.jsonm -Dexec.args="in.json"
```