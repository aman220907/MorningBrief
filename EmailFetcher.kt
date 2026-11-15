package com.morningbrief.app

import com.sun.mail.imap.IMAPFolder
import java.util.Date
import java.util.Properties
import javax.mail.FetchProfile
import javax.mail.Flags
import javax.mail.Folder
import javax.mail.Session
import javax.mail.Store
import javax.mail.internet.InternetAddress
import javax.mail.search.AndTerm
import javax.mail.search.ComparisonTerm
import javax.mail.search.FlagTerm
import javax.mail.search.ReceivedDateTerm

/**
 * Reads UNREAD emails that Gmail has marked "Important", from the last few days,
 * using Gmail's IMAP server. Emails are only read, never changed or marked as read.
 */
object EmailFetcher {

    data class Mail(val from: String, val subject: String)
    data class Result(val total: Int, val items: List<Mail>)

    class NoImportantFolderException : Exception(
        "Couldn't find Gmail's Important folder. In Gmail on a computer: " +
            "Settings > Labels > Important > turn on \"Show in IMAP\"."
    )

    fun fetchImportantUnread(user: String, password: String, days: Int, max: Int): Result {
        val props = Properties()
        props.setProperty("mail.store.protocol", "imaps")
        props.setProperty("mail.imaps.ssl.enable", "true")
        props.setProperty("mail.imaps.connectiontimeout", "15000")
        props.setProperty("mail.imaps.timeout", "30000")

        val session = Session.getInstance(props)
        val store = session.getStore("imaps")
        store.connect("imap.gmail.com", 993, user, password)
        try {
            val folder = findImportantFolder(store) ?: throw NoImportantFolderException()
            folder.open(Folder.READ_ONLY)
            try {
                val cutoff = Date(System.currentTimeMillis() - days * 24L * 60L * 60L * 1000L)
                val term = AndTerm(
                    FlagTerm(Flags(Flags.Flag.SEEN), false),
                    ReceivedDateTerm(ComparisonTerm.GE, cutoff)
                )
                val found = folder.search(term)

                val profile = FetchProfile()
                profile.add(FetchProfile.Item.ENVELOPE)
                folder.fetch(found, profile)

                val newestFirst = found.sortedByDescending { it.receivedDate?.time ?: 0L }
                val items = newestFirst.take(max).map { m ->
                    val addr = m.from?.firstOrNull()
                    val from = when (addr) {
                        null -> "Unknown sender"
                        is InternetAddress ->
                            addr.personal?.takeIf { it.isNotBlank() } ?: addr.address
                        else -> addr.toString()
                    }
                    val subject = m.subject?.takeIf { it.isNotBlank() } ?: "(no subject)"
                    Mail(from, subject)
                }
                return Result(found.size, items)
            } finally {
                folder.close(false)
            }
        } finally {
            store.close()
        }
    }

    private fun findImportantFolder(store: Store): Folder? {
        // Gmail tags the right folder with the special "\Important" attribute,
        // which works even if your Gmail language isn't English.
        for (f in store.defaultFolder.list("*")) {
            if (f is IMAPFolder) {
                if (f.attributes.any { it.equals("\\Important", ignoreCase = true) }) return f
            }
        }
        val fallback = store.getFolder("[Gmail]/Important")
        return if (fallback.exists()) fallback else null
    }
}
