package com.enfotrix.life_changer_user_2_0.Pdf


import com.enfotrix.life_changer_user_2_0.Models.TransactionModel
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.FontFactory
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class PdfAllTransactions(val filteredApprovedInvesmentList: List<TransactionModel>, val firstName: String) {
    fun generatePdf(outputStream: OutputStream): Boolean {
        val document = Document()

        try {
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, outputStream)
            document.open()
            val titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f, BaseColor.BLACK)

            val title: Paragraph = Paragraph("$firstName Earnings ", titleFont)


            title.alignment = Element.ALIGN_CENTER
            document.add(title)
            document.add(Paragraph("\n"))

            val table = PdfPTable(4)
            table.widthPercentage = 100f
            val headers = arrayOf(
                Paragraph("Deposit Balance", titleFont),
                Paragraph("Old Balance", titleFont),
                Paragraph("Deposit Date", titleFont),
                Paragraph("Approval Date", titleFont)
            )
            for (header in headers) {
                val cell = PdfPCell(Phrase(header))
                cell.horizontalAlignment = Element.ALIGN_CENTER
                table.addCell(cell)
            }
            for (item in filteredApprovedInvesmentList) {
                table.addCell(item.amount)
                table.addCell(item.previousBalance)
                table.addCell(SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(item.createdAt.toDate()))
                val transactionAt = item.transactionAt
                if (transactionAt != null) {
                    table.addCell(SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(transactionAt.toDate()))
                } else {
                    table.addCell("pending")
                }
            }

            document.add(table)
            document.close()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
