import org.apache.lucene.document.{StringField, TextField, Document, Field}
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.util.Version
import java.io.{FileReader, File}

class Indexer (indexDir: String) {

  def dir:Directory = FSDirectory.open(new File(indexDir))
  def iwc:IndexWriterConfig = new IndexWriterConfig(
          Version.LUCENE_46,
          new StandardAnalyzer(Version.LUCENE_46)
  )
  iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE)
  var writer = new IndexWriter (dir,iwc);


  def index (dataDir: String, filter:File=>Boolean) :Int ={
        def files: Array[File] = new File(dataDir).listFiles();

        for (f <- files) {
            if (!f.isDirectory() &&
                    !f.isHidden() &&
                    f.exists() &&
                    f.canRead() &&
                    (filter == null || filter(f))) {
                indexFile(f);
            }
        }
        return writer.numDocs();
  }

  def getDocument(f:File) :Document = {
    var doc:Document = new Document();
    doc.add(new TextField("contents", new FileReader(f)));
    doc.add(new StringField("filename", f.getName(),
                Field.Store.YES));
    doc.add(new StringField("fullpath", f.getCanonicalPath(),
                Field.Store.YES));

    return doc;
  }

  def indexFile(f: File) {
      System.out.println("Indexing " + f.getCanonicalPath());
      val doc:Document = getDocument(f);
      writer.addDocument(doc);
  }

  def close() {
    writer.close();
  }
}

object Index extends App {
  def textFilesFilter(f:File) = f.getName().toLowerCase.endsWith("txt")

  def createIndex(indexDir:String, dataDir:String){

    val start:Long = System.currentTimeMillis();
    val indexer:Indexer = new Indexer(indexDir);
    val numIndexed = (n:Int) => n;
      try {
          numIndexed(indexer.index(dataDir, textFilesFilter));
      } finally {
          indexer.close();
      }
    val end:Long = System.currentTimeMillis();
    System.out.println("Indexing " + numIndexed + " files took "
              + (end - start) + " milliseconds");
  }

  if (args.length != 2) {
    throw new IllegalArgumentException("Usage: Index "
      + " <index dir> <data dir>");
  }
  createIndex(args(0), args(1))
}


