package models
import play._
import java.time._
import slick.collection.heterogeneous.{ HList, HCons, HNil }
import slick.collection.heterogeneous.syntax._
import scala.language.postfixOps

final case class Record(
  dt:    LocalDateTime,
  ch:    Int,
  stat:  Int,
  v:     Seq[Double],
  flow:  Double,
  coeff: Int) {
  def toHList = dt :: ch :: stat ::
    v(0) :: v(1) :: v(2) :: v(3) :: v(4) ::
    v(5) :: v(6) :: v(7) :: v(8) :: v(9) ::
    v(10) :: v(11) :: v(12) :: v(13) :: v(14) ::
    v(15) :: v(16) :: v(17) :: v(18) :: v(19) ::  flow :: coeff :: HNil
}

object Record {
  type RecordHList = LocalDateTime :: Int :: Int :: Double :: Double :: Double :: Double :: Double :: Double :: Double :: Double :: Double :: Double :: Double :: Double :: Double :: Double :: Double :: Double :: Double :: Double :: Double :: Double :: Double ::  Int :: HNil

  def fromRecord(rec: Record): Option[RecordHList] = Some(rec.toHList)
  def intoRecord(hl: RecordHList): Record = {
    val values = Seq(hl(3), hl(4), hl(5), hl(6), hl(7),
      hl(8), hl(9), hl(10), hl(11), hl(12),
      hl(13), hl(14), hl(15), hl(16), hl(17),
      hl(19), hl(20), hl(21), hl(22), hl(23))
    Record(hl(0), hl(1), hl(2), values, hl(23), hl(24))
  }

}