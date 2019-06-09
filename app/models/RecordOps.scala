package models
import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.collection.heterogeneous.{ HList, HCons, HNil }
import slick.collection.heterogeneous.syntax._
import scala.language.postfixOps
import scala.concurrent._
import play._

/**
 * A repository for people.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this for you.
 */
@Singleton
class RecordOps @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import profile.api._
  import java.time._

  implicit val localDateTimeToTimestamp = MappedColumnType.base[LocalDateTime, java.sql.Timestamp](
    l => java.sql.Timestamp.valueOf(l),
    t => t.toLocalDateTime)


  class RawDataTable(tag: Tag) extends Table[Record](tag, "Data_Log") {
    def ch = column[Int]("Ch_No")
    def status = column[Int]("Data_St")
    def v1 = column[Float]("AN01")
    def v2 = column[Float]("AN02")
    def v3 = column[Float]("AN03")
    def v4 = column[Float]("AN04")
    def v5 = column[Float]("AN05")
    def v6 = column[Float]("AN06")
    def v7 = column[Float]("AN07")
    def v8 = column[Float]("AN08")
    def v9 = column[Float]("AN09")
    def v10 = column[Float]("AN10")
    def v11 = column[Float]("AN11")
    def v12 = column[Float]("AN12")
    def v13 = column[Float]("AN13")
    def v14 = column[Float]("AN14")
    def v15 = column[Float]("AN15")
    def v16 = column[Float]("AN16")
    def v17 = column[Float]("AN17")
    def v18 = column[Float]("AN18")
    def v19 = column[Float]("AN19")
    def v20 = column[Float]("AN20")
    def mfc = column[Float]("MFC")
    def flow = column[Float]("Flow")
    def coeff = column[Int]("Coeff")
    def dt = column[LocalDateTime]("DataTime")
    
    def pk = primaryKey("pk_id", (dt, ch))

    def * = (ch :: status ::
      v1 :: v2 :: v3 :: v4 :: v5 ::
      v6 :: v7 :: v8 :: v9 :: v10 ::
      v11 :: v12 :: v13 :: v14 :: v15 ::
      v16 :: v17 :: v18 :: v19 :: v20 :: mfc :: flow :: coeff :: dt ::  HNil) <> (Record.intoRecord, Record.fromRecord)
  }

  /**
   * The starting point for all queries on the people table.
   */
  lazy val records = TableQuery[RawDataTable]

  private def init() = {
    import slick.jdbc.meta.MTable
    import scala.concurrent.duration._
    import scala.concurrent.Await

    for (tables <- db.run(MTable.getTables)) {
      if (!tables.exists(table => table.name.name == "Data_Log")) {
        Logger.info("Create Data_Log tab")
        db.run(records.schema.create)
      }
    }
  }

  init
  /**
   * Create a person with the given name and age.
   *
   * This is an asynchronous operation, it will return a future of the created person, which can be used to obtain the
   * id for that person.
   */

  def create(rec: Record) = db.run {
    records += rec
    //    // We create a projection of just the name and age columns, since we're not inserting a value for the id column
    //    (records.map(p => (p.dt, p.mt, p.value, p.status))
    //      // Now define it to return the id, because we want to know what id was generated for the person
    //      returning records.map(r => (r.dt, r.mt))
    //      // And we define a transformation for the returned value, which combines our original parameters with the
    //      // returned id
    //      into ((nameAge, id) => Person(id, nameAge._1, nameAge._2))
    //    // And finally, insert the person into the database
    //    ) += (name, age)
  }

  def addDummyRecord() = {

    //create(LocalDateTime.now(), "test", 0, "123")
  }
  /**
   * List all the people in the database.
   */
  def list(): Future[Seq[Record]] = db.run {
    records.result
  }

  def getHistoryData(start: LocalDateTime, end: LocalDateTime) = db.run {
    records.filter(r => r.dt >= start && r.dt < end).result
  }
}