package demo.app.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.firefly.db.annotation.Column
import com.firefly.db.annotation.Id
import com.firefly.db.annotation.Table
import java.net.URL
import java.util.*


@Table(value = "user", catalog = "test")
data class User @JsonCreator @JvmOverloads constructor(@JsonProperty("id") @Id("id") var id: Long? = null,
                                         @JsonProperty("firstName")@Column("pt_first_name") var firstName: String = "",
                                         @JsonProperty("lastName")@Column("pt_last_name") var lastName: String = "",
                                         @JsonProperty("avatarUrl")@Column("pt_avatar_url") var avatarUrl: String? = null) {

}

data class Avatar @JsonCreator constructor(@JsonProperty("url") val url: String)