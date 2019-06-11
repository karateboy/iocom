package models
import javax.inject.{ Inject, Singleton }
import scala.language.postfixOps
import scala.concurrent._
import play._
import scalikejdbc._

/**
 * A repository for people.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this for you.
 */

@Singleton
class RecordOps2 @Inject() ()(implicit ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  import java.time._

  private def init() = {
  }

  init

  def insert(rec: Record) = {
    DB localTx {
      implicit session =>
        sql"""
          Insert into Data_Log(Ch_No, Data_St, 
                AN01, AN02, AN03, AN04, AN05, AN06, AN07, AN08, AN09, AN10, 
                AN11, AN12, AN13, AN14, AN15, AN16, AN17, AN18, AN19, AN20,
                MFC, Flow, Coeff, DataTime) values 
                (${rec.ch}, ${rec.stat}, 
                ${rec.v(0)}, ${rec.v(1)}, ${rec.v(2)}, ${rec.v(3)}, ${rec.v(4)}, 
                ${rec.v(5)}, ${rec.v(6)}, ${rec.v(7)}, ${rec.v(8)}, ${rec.v(9)},
                ${rec.v(10)}, ${rec.v(11)}, ${rec.v(12)}, ${rec.v(13)}, ${rec.v(14)}, 
                ${rec.v(15)}, ${rec.v(16)}, ${rec.v(17)}, ${rec.v(18)}, ${rec.v(19)},
                ${rec.mfc}, ${rec.flow}, ${rec.coeff}, ${java.sql.Timestamp.valueOf(rec.dt)})
          """.update.apply()
    }
  }

  def getHistoryData(start: LocalDateTime, end: LocalDateTime) = DB localTx {
    implicit session =>
      sql"""
        Select *
        From Data_Log
        Where DataTime >= $start and DataTime < $end
        """.map(rs => {

        val values =
          for (i <- 1 to 20) yield rs.float("AN%02d".format(i))

        Record(
          ch = rs.int("Ch_No"),
          stat = rs.int("Data_St"),
          v = values,
          mfc = rs.float("MFC"),
          flow = rs.float("Flow"),
          coeff = rs.int("Coeff"),
          dt = rs.dateTime("DataTime").toLocalDateTime())
      }).list.apply
  }
}