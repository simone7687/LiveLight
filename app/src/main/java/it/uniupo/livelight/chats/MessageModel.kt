package it.uniupo.livelight.chats

data class MessageModel(val id: String) {
    lateinit var message: String
    lateinit var senderId: String
    lateinit var receiverId: String
    lateinit var receiverName: String
    lateinit var dateTime: String
    lateinit var senderName: String
    var isSender: Boolean = false
}