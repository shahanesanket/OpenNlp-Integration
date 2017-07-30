# OpenNlp-Integration
Sample codes to use openNlp with Apache Spark &amp; Lucidworks fusion. Especially the Parts of Speech Tagging module with SPARK RDD's.

Fusion is already shipped with some binaries that openNlp uses to tokenize, tag etc. ```fusion/3.1.0/data/nlp/models/``` directory contains the required binaries.

The opennlp-tools-1.*.jar is likely to be shipped with the fusion.jar. Hence, we can directly import opennlp tools in Scala scripts and run them in `./spark-shell`.

The `opennlp_integration.scala` is the working example of the using openNLP inside of the spark-shell. We load the data from Solr as Spark DataFrame and define tokenizer and tagger functions to perform the POS-tagging on the text data in the rdd.

**Some warnings: VERY IMPORTANT ALWAYS USE `File` function to load the `.bin` files. Nothing else! 
 Do not use FileInputStream, FileReader, or getClass.getResource(), or getClass.getResourceAsStream(). You may run into TaskNotSerializable when applying these functions to RDDs.**
 
Currently we are loading these files inside of the functions. If we do it outside we'll get serialization errors and it won't work with RDDs. This could potentially have some performance issues because, when we call map on the rdd we are calling the tokenize,tag functions which read the binary file. Looks like a lot of disk reads.

The project that we were referring wasn't doing anything really useful; just adding another layer of scala code which calls opennlp jar.

### Execution instructions:
1. Start the spark shell
`./spark-shell`

2. Copy paste the contents from `opennlp_integration.scala` on the console and see the output
