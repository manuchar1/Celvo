import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MarketingBannerDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
    @SerialName("assetUrl") val assetUrl: String,
    @SerialName("action") val action: BannerActionDto,
    @SerialName("style") val style: BannerStyleDto
)

@Serializable
data class BannerActionDto(
    @SerialName("label") val label: String,
    @SerialName("deepLink") val deepLink: String
)

@Serializable
data class BannerStyleDto(
    @SerialName("backgroundColor") val backgroundColor: String,
    @SerialName("textColor") val textColor: String,
    @SerialName("type") val type: String
)