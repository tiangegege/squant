package com.squant.cheetah.engine

import java.time.LocalDateTime

import com.squant.cheetah.utils._

sealed trait ClockType {
  def now(): LocalDateTime

  def update()

  def isFinished(): Boolean

  def interval(): Int
}

case class BACKTEST(start: LocalDateTime, stop: LocalDateTime, unit: Int) extends ClockType {
  var currentTime = start

  assert(start.isBefore(stop))

  override def update() = {
    val tmpTime = currentTime.plusMinutes(unit)
    if (!isTradingTime(tmpTime) && tmpTime.getHour >= 15) {
      currentTime = currentTime.plusDays(1).withHour(9).withMinute(30).withSecond(0)
    } else {
      currentTime = tmpTime
    }
  }

  override def now(): LocalDateTime = {
    currentTime
  }

  def interval() = unit

  override def isFinished(): Boolean = currentTime.isAfter(stop)
}

case class LIVE(i: Int) extends ClockType {

  override def now(): LocalDateTime = LocalDateTime.now()

  def interval() = i

  override def isFinished(): Boolean = false

  override def update(): Unit = {}
}

class Clock(cType: ClockType) {

  def now(): LocalDateTime = cType.now()

  def getRange(count: Int): (LocalDateTime, LocalDateTime) = {
    val date = cType.now
    (date.plusMinutes(-count), date)
  }

  def clockType(): ClockType = cType

  def interval() = cType.interval()

  def update() = {
    cType.update()
  }

  def isFinished(): Boolean = cType.isFinished()


  override def toString = s"Clock($now, $clockType, $interval)"
}

object Clock {
  /**
    *
    * @param interval 单位:分钟
    * @param start
    * @param stop
    * @return
    */
  def mk(interval: Int = 1, start: Option[LocalDateTime] = None, stop: Option[LocalDateTime] = Option[LocalDateTime](LocalDateTime.now())): Clock = {
    start match {
      case None => new Clock(LIVE(interval))
      case Some(t) => new Clock(BACKTEST(start.get, stop.get, interval))
    }
  }
}