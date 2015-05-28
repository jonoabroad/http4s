package org.http4s
package multipart

import org.http4s._

import org.http4s.MediaType._
import org.http4s.headers._
import org.http4s.Http4s._
import org.http4s.Uri._
import org.http4s.util._
import org.http4s.Status.Ok

import scodec.bits.ByteVector
import org.http4s.EntityEncoder._
import Entity._
import scalaz.stream.Process
import org.specs2.Specification
import org.specs2.scalaz.DisjunctionMatchers

class MultipartSpec extends Specification  with DisjunctionMatchers {

  def is = s2"""
    Multipart form data can be 
      encoded and decoded example A       $encodeAndDecodeA
      encoded and decoded example B       $encodeAndDecodeB
     """

  def encodeAndDecodeA = {

   implicit def mpe: EntityEncoder[Multipart] = MultipartEntityEncoder

    val url = Uri(
      scheme = Some(CaseInsensitiveString("https")),
      authority = Some(Authority(host = RegName("example.com"))),
      path = "/path/to/some/where")

    val txtToEntity: String => EntityEncoder.Entity = in =>
        EntityEncoder.Entity(Process.emit(in).map(s => ByteVector(s.getBytes)))


    val ctf1       = Some(`Content-Type`(`text/plain`))
    val ef1        = txtToEntity("Text_Field_1")
    val field1     = FormData(Name("field1"), ctf1, ef1)
    val ef2        = txtToEntity("Text_Field_2")
    val field2     = FormData(Name("field2"), None, ef2)
    val multipart  = Multipart(List(field1,field2))
    val entity     = MultipartEntityEncoder.toEntity(multipart)
    val body       = entity.run.body.runLog.run.fold(ByteVector.empty)((acc,x) => acc ++ x )
    val request    = Request(method  = Method.POST,
                             uri     = url,
                             body    = Process.emit(body),
                             headers = multipart.headers )
          
    val decoder    = MultipartEntityDecoder.decoder
    val decoded    = decoder.decode(request)
    val result     = decoded.run.run

    
    result must beRightDisjunction(multipart)
  }

  def encodeAndDecodeB = {

   implicit def mpe: EntityEncoder[Multipart] = MultipartEntityEncoder

    val url   = Uri(
      scheme    = Some(CaseInsensitiveString("https")),
      authority = Some(Authority(host = RegName("example.com"))),
      path      = "/path/to/some/where")

    val txtToEntity: String => EntityEncoder.Entity = in =>
        EntityEncoder.Entity(Process.emit(in).map(s => ByteVector(s.getBytes)))


    val ef1        = txtToEntity("Text_Field_1")
    val field1     = FormData(Name("field1"), None, ef1)
    val multipart  = Multipart(List(field1))

    val entity     = MultipartEntityEncoder.toEntity(multipart)
    val body       = entity.run.body.runLog.run.fold(ByteVector.empty)((acc,x) => acc ++ x )
    val request    = Request(method  = Method.POST,
                             uri     = url,
                             body    = Process.emit(body),
                             headers = multipart.headers )
          
    val decoder    = MultipartEntityDecoder.decoder
    val decoded    = decoder.decode(request)
    val result     = decoded.run.run

    
    result must beRightDisjunction(multipart)
  }

  
}