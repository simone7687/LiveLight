package it.uniupo.livelight.chats

data class MessageModel(val id: String) {
    lateinit var message: String
    lateinit var dateTime: String
    var isSender: Boolean = false
}