package it.uniupo.livelight.post

data class PostModel(val id: String) {
    lateinit var user: String
    lateinit var title: String
    lateinit var description: String
    lateinit var datePosted: String
    lateinit var image: String
}