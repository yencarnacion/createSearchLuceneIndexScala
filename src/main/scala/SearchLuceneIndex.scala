import java.io.File
import org.apache.lucene.analysis.core.SimpleAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexReader
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.TopDocs
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.Version
import org.apache.lucene.queryparser.classic.QueryParser

class Searcher(indexLocation: String) {
  var reader:IndexReader = DirectoryReader.open(FSDirectory.open(new File(indexLocation)));
  // Now search the index
  var searcher:IndexSearcher = new IndexSearcher(reader);

  def searchIndex(query:String) = {
    // Parse a simple query that searches for "text":
    val parser:QueryParser = new QueryParser(Version.LUCENE_46, "contents", new SimpleAnalyzer(Version.LUCENE_46));
    val q:Query = parser.parse(query);
    val docs:TopDocs = searcher.search(q, 10);
    System.out.println("Total hits: " +docs.totalHits);
    val doc:Document=searcher.doc(docs.scoreDocs(0).doc);
    System.out.println(doc.get("filename"));
  }

  def close = reader.close()
}

object Search extends App {
  if (args.length != 2) {
    throw new IllegalArgumentException("Usage: Search "
      + "<indexDir> <query>");
  }

  val searcher:Searcher = new Searcher(args(0));

  searcher.searchIndex(args(1))
}