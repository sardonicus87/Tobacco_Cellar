package com.sardonicus.tobaccocellar.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "items",
    indices = [Index(value = (["brand", "blend"]), unique = true)]
)
data class Items(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val brand: String,
    val blend: String,
    val type: String,
    val quantity: Int,
    val favorite: Boolean,
    val disliked: Boolean,
    val notes: String,
    val subGenre: String,
    val cut: String,
    val inProduction: Boolean,
)

@Entity(
    tableName = "tins",
    foreignKeys = [
        ForeignKey(
            entity = Items::class,
            parentColumns = ["id"],
            childColumns = ["itemsId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = (["itemsId", "tinLabel"]), unique = true)]
)
data class Tins(
    @PrimaryKey(autoGenerate = true)
    val tinId: Int = 0,
    val itemsId: Int,
    val tinLabel: String,
    val container: String,
    val tinQuantity: Double,
    val unit: String,
    val manufactureDate: Long?,
    val cellarDate: Long?,
    val openDate: Long?,
    val finished: Boolean,
)

@Entity(
    tableName = "components",
    indices = [Index(value = (["componentName"]), unique = true)]
)
data class Components(
    @PrimaryKey(autoGenerate = true)
    val componentId: Int = 0,
    val componentName: String,
)

@Entity(
    tableName = "items_components_cross_ref",
    primaryKeys = ["itemId", "componentId"],
    foreignKeys = [
        ForeignKey(
            entity = Items::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Components::class,
            parentColumns = ["componentId"],
            childColumns = ["componentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = (["componentId"]))]
)
data class ItemsComponentsCrossRef(
    val itemId: Int,
    val componentId: Int,
)

@Entity(
    tableName = "flavoring",
    indices = [Index(value = (["flavoringName"]), unique = true)]
)
data class Flavoring(
    @PrimaryKey(autoGenerate = true)
    val flavoringId: Int = 0,
    val flavoringName: String,
)

@Entity(
    tableName = "items_flavoring_cross_ref",
    primaryKeys = ["itemId", "flavoringId"],
    foreignKeys = [
        ForeignKey(
            entity = Items::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Flavoring::class,
            parentColumns = ["flavoringId"],
            childColumns = ["flavoringId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = (["flavoringId"]))]
)
data class ItemsFlavoringCrossRef(
    val itemId: Int,
    val flavoringId: Int,
)


data class ItemsComponentsAndTins(
    @Embedded val items: Items,
    @Relation(
        parentColumn = "id",
        entityColumn = "componentId",
        associateBy = Junction(
            value = ItemsComponentsCrossRef::class,
            parentColumn = "itemId",
            entityColumn = "componentId"
        )
    ) val components: List<Components> = emptyList(),
    @Relation(
        parentColumn = "id",
        entityColumn = "flavoringId",
        associateBy = Junction(
            value = ItemsFlavoringCrossRef::class,
            parentColumn = "itemId",
            entityColumn = "flavoringId"
        )
    ) val flavoring: List<Flavoring> = emptyList(),
    @Relation(
        parentColumn = "id",
        entityColumn = "itemsId"
    ) val tins: List<Tins>,
)


data class ItemsWithComponentsAndFlavoring(
    val item: Items,
    val components: List<Components> = emptyList(),
    val flavoring: List<Flavoring> = emptyList(),
)


data class TinExportData(
    val brand: String,
    val blend: String,
    val type: String,
    val subGenre: String,
    val cut: String,
    val favorite: Boolean,
    val disliked: Boolean,
    val inProduction: Boolean,
    val notes: String,
    val components: String,
    val flavoring: String,
    val container: String,
    val quantity: String,
    val manufactureDate: String,
    val cellarDate: String,
    val openDate: String,
    val finished: Boolean,
)