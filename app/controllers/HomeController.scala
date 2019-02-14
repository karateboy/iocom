package controllers

import javax.inject._

import play.api.mvc._
import models._
import play._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json._
/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() (plc: OmronPlc, recordOps: RecordOps, cc: ControllerComponents)(implicit assetsFinder: AssetsFinder)
  extends Authentication(cc) {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    Redirect("/app/index.html")
  }

  def realtimeData = Authenticated.async {
    plc.getRealtimeData map {
      realtimeData =>
        implicit val mtWrite = Json.writes[MonitorType]
        implicit val write = Json.writes[ShownRecord]
        val shownData = realtimeData map { rec => ShownRecord(rec.mt, rec.mt.formatRecord(rec.value)) }
        Ok(Json.toJson(shownData))
    }
  }

  def getMonitorTypes = Authenticated {
    implicit val mtWrite = Json.writes[MonitorType]
    Ok(Json.toJson(plc.mtList))
  }

  import java.time._
  case class CellData(v: String, cellClassName: String)
  case class RowData(date: LocalDateTime, cellData: Seq[CellData])
  case class DataTab(columnNames: Seq[String], rows: Seq[RowData])

  def getHistoryData(monitorTypeStr: String, start: Long, end: Long) = Authenticated.async {

    val startLT = LocalDateTime.ofInstant(Instant.ofEpochMilli(start), ZoneId.systemDefault())
    val endLT = LocalDateTime.ofInstant(Instant.ofEpochMilli(end), ZoneId.systemDefault())

    implicit val cdWrite = Json.writes[CellData]
    implicit val rdWrite = Json.writes[RowData]
    implicit val dtWrite = Json.writes[DataTab]

    val mtStr1 = java.net.URLDecoder.decode(monitorTypeStr, "UTF-8")
    val monitorTypes = mtStr1.split(",")
    //val monitorTypes = monitorTypeStr.split(",")
    Logger.debug(monitorTypes.toString())
    val columnNames = monitorTypes.+:("CH")
    Logger.debug(columnNames.toString())
    for (rowData <- recordOps.getHistoryData(startLT, endLT)) yield {
      val rows = rowData map {
        row =>
          val chCellData = CellData(row.ch.toString, "")
          val mtCellData = monitorTypes.toSeq map { mt => plc.mtCaseMap(mt).formatRecord(Some(row.v(plc.mtIdxMap(mt)))) } map { CellData(_, "") }
          RowData(row.dt, mtCellData.+:(chCellData))
      }
      Ok(Json.toJson(DataTab(columnNames, rows)))
    }

  }
}
