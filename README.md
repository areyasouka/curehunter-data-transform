# CureHunter Data Transformer Example Java CLI 

Example of CLI XSL transformer for licensed [CureHunter](https://curehunter.com) data.
Multithreaded conversion of CureHunter sentence relationship and MeSH keyword tagged NLM PubMed Data ".xml.gz" files to TSV etc.

"medlineCitationTSV.xsl" extracts all medline citation abstracts from the sample "./data/pubmed-sample*.xml.gz" pubmed xml files which contain Drug-Disease relationships.

## Usage

Run application through Maven

```
$ mkdir output
$ ./mvnw -Dexec.args=transform
```

Run tests & build an executable JAR:

```
$ ./mvnw package
```

Run tests as native image & build a native executable:

```
$ ./mvnw package -Pnative
```

Original Template [java-cli-project-template](https://github.com/maciejwalkowiak/java-cli-project-template)! Thank you!
