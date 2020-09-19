package it.uniupo.livelight.chats

data class ChatModel(val id: String) {
    lateinit var title: String
    lateinit var users: ArrayList<String>
    lateinit var image: String
    lateinit var owner: String
}