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

  /**
   * Here we define the table. It will have a name of people
   */
  class RawDataTable(tag: Tag) extends Table[Record](tag, "rawData") {
    def dt = column[LocalDateTime]("DT")
    def ch = column[Int]("Ch")
    def status = column[Int]("status")
    def v1 = column[Double]("v1")
    def v2 = column[Double]("v2")
    def v3 = column[Double]("v3")
    def v4 = column[Double]("v4")
    def v5 = column[Double]("v5")
    def v6 = column[Double]("v6")
    def v7 = column[Double]("v7")
    def v8 = column[Double]("v8")
    def v9 = column[Double]("v9")
    def v10 = column[Double]("v10")
    def v11 = column[Double]("v11")
    def v12 = column[Double]("v12")
    def v13 = column[Double]("v13")
    def v14 = column[Double]("v14")
    def v15 = column[Double]("v15")
    def v16 = column[Double]("v16")
    def v17 = column[Double]("v17")
    def v18 = column[Double]("v18")
    def v19 = column[Double]("v19")
    def v20 = column[Double]("v20")
    def flow = column[Double]("flow")
    def coeff = column[Int]("coeff")

    def pk = primaryKey("pk_id", (dt, ch))

    def * = (dt :: ch :: status ::
      v1 :: v2 :: v3 :: v4 :: v5 ::
      v6 :: v7 :: v8 :: v9 :: v10 ::
      v11 :: v12 :: v13 :: v14 :: v15 ::
      v16 :: v17 :: v18 :: v19 :: v20 :: flow :: coeff :: HNil) <> (Record.intoRecord, Record.fromRecord)
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
      if (!tables.exists(table => table.name.name == "rawData")) {
        Logger.info("Create rawData tab")
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