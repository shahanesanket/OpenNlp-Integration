/********************************************************************************************************* 
 * openNlp - Spark Tutorial.
 * Script to integrate openNLP into spark-shell. Fusion is already shipped with some binaries that openNlp uses to tokenize, tag etc.
 * fusion/3.1.0/data/nlp/models/ directory contains the required binaries.
 *
 ********************************************************************************************************* 
 VERY IMPORTANT ALWAYS USE File function only to load the .bin files. Nothing else! 
 Do not use FileInputStream, FileReader, or getClass.getResource(), or getClass.getResourceAsStream()

 Currently we are loading these files inside of the functions. If we do it outside we'll get serialization errors and it won't work with RDDs. 
 The project that we were referring wasn't doing anything really useful; just adding another layer of scala code which calls opennlp tokenizers and other functions.
 *********************************************************************************************************
*/

import java.io.File
import opennlp.tools.tokenize.{TokenizerME, TokenizerModel}
import opennlp.tools.postag.{POSModel, POSTaggerME}
import com.lucidworks.spark.rdd.SolrRDD
import org.apache.spark.rdd.RDD

// get the binaries directory from the current working directory i.e. from fusion/3.1.0/bin
var currentDir = new File("").getAbsolutePath()
var binariesDir = currentDir+"/../data/nlp/models/"

// Function for load the tokenizer from the binary file and tokenizing the sentence.
def tokenize(sentence:String):List[String] = {
	var modelIn = new File(binariesDir+"en-token-1.bin")
	var model = new TokenizerModel(modelIn)
	val tokenizer = new TokenizerME(model)
	tokenizer.tokenize(sentence).toList
}

// Function for load the POSTagger from the binary file and tag the tokens.
def tag(tokens:List[String]):List[(String, String)] = {
	var modelIn = new File(binariesDir+"en-pos-maxent.bin")
	var model = new POSModel(modelIn)
	val tagger = new POSTaggerME(model)
	var tags = tagger.tag(tokens.toArray)
	tokens.zip(tags)
}

/* Now let's try loading data from fusion as a DataFrame and see how we can use the tokenizers and pos tagger. */
val sqlContext = spark.sqlContext
val options = Map(
  "collection" -> "lucidfind",
  "zkhost" -> "localhost:9983/lwfusion/3.1.0/solr"
)

var rawData = spark.read.format("solr").options(options).load().select("hash_id","subject")
// tag and tokenize
rawData.rdd.map{case c=> (c.getAs[String](0), tag(tokenize(c.getAs[String](1))) ) }
// another way to do the same thing
rawData.rdd.map{case c=> (c.getAs[String](0), tokenize(c.getAs[String](1)))}.map{case c=> (c._1,tag(c._2))}


// Similar to the above example we can work with all the options OpenNLP tools have to offer.

// One more example of sentence detector:
import opennlp.tools.sentdetect.{SentenceDetectorME, SentenceModel}
def sentenceDetect(paragraph:String):List[String] = {
	var modelIn = new File(binariesDir+"en-sent.bin") //just put the absolute bin file path.
	var model = new SentenceModel(modelIn)
	val sentDetector = new SentenceDetectorME(model)
	var sentences = sentDetector.sentDetect(paragraph)
	sentences.toList
}

sentenceDetect("This is sentence one. Sentence 2 follows and naturally, sentence 3. Awesome its working")
