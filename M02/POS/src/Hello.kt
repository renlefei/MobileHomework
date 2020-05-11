fun main() {
    val receipt = getReceipt()
    println(receipt)
    val result = when (receipt == expectedReceipt) {
        true -> "正确 ✅"
        false -> "错误 ❌"
    }
    println("\n结果：${result}")
}

interface Promotion {
    var barcodes: List<String>
}

data class BuyTwoGetOneFreePromotion(override var barcodes: List<String>) : Promotion {
}

fun loadPromotions(): List<Promotion> = listOf(BuyTwoGetOneFreePromotion(listOf("ITEM000000", "ITEM000001", "ITEM000005")))

data class Item(val barcode: String, val name: String, val unit: String, val price: Double) {

}

data class ItemReceipt(val name: String, val quantity: Int, val price: Double, val Unit: String, val savings: Double, val subtotal: Double){
    val itemResult: String = "名称：$name，数量：$quantity$Unit，单价：" +
            String.format("%.2f", price) + "(元)，小计：" +
            String.format("%.1f", subtotal) + "(元)"
}

data class Receipt(val itemsReceipt: List<ItemReceipt>){
    private val total = itemsReceipt.map { it.subtotal}.sum()
    private var totalSavings = itemsReceipt.map { it.savings }.sum()
    private var content = itemsReceipt.map { itemReceipt -> itemReceipt.itemResult }.joinToString("\n")
    val result = "\n***<没钱赚商店>收据***\n" +
            content +
            "\n----------------------\n" +
            "总计：" + String.format("%.2f", total) + "(元)\n" +
            "节省：" + String.format("%.2f", totalSavings) +
            "(元)\n" + "**********************\n"
}

fun loadAllItems(): List<Item> {
    return listOf(
        Item("ITEM000000", "可口可乐", "瓶", 3.00),
        Item("ITEM000001", "雪碧", "瓶", 3.00),
        Item("ITEM000002", "苹果", "斤", 5.50),
        Item("ITEM000003", "荔枝", "斤", 15.00),
        Item("ITEM000004", "电池", "个", 2.00),
        Item("ITEM000005", "方便面", "袋", 4.50)
    )
}

val purchasedBarcodes = listOf(
    "ITEM000001",
    "ITEM000001",
    "ITEM000001",
    "ITEM000001",
    "ITEM000001",
    "ITEM000003-2",
    "ITEM000005",
    "ITEM000005",
    "ITEM000005"
)

fun getPromotionInfo(promotions:List<Promotion>, barcode: String, price:Double, quantity: Int): Pair<Double, Double> {
    val promotion = promotions.firstOrNull { item -> item.barcodes.contains(barcode) }
    return if(promotion != null){
        var savings = price * (quantity/3)
        var subtotal = price * quantity - savings
        subtotal to savings
    }else{
        var savings = 0.0
        var subtotal = price * quantity - savings
        subtotal to savings
    }
}



fun getReceipt(): String {
    var purchasedGroupedBarcodes: MutableMap<String, Int> = getOrderInfo(purchasedBarcodes)

    val itemsReceipt = mutableListOf<ItemReceipt>()

    val allItems = loadAllItems()
    val promotions = loadPromotions()

    for((k,v) in purchasedGroupedBarcodes) {

        val targetItem = allItems.first(){item -> item.barcode == k}

        val subtotal = getPromotionInfo(promotions, k, targetItem.price, v).first
        val savings = getPromotionInfo(promotions, k, targetItem.price, v).second


        val itemReceipt = ItemReceipt(name = targetItem.name, quantity = v, price = targetItem.price, Unit = targetItem.unit, savings = savings, subtotal = subtotal)
        itemsReceipt.add(itemReceipt)

    }

    return Receipt(itemsReceipt).result
}

fun getOrderInfo(purchasedBarcodes: List<String>): MutableMap<String, Int> {
    var purchasedGroupedBarcodes: MutableMap<String, Int> = mutableMapOf()

    for (item in purchasedBarcodes) {
        when {
            item.contains('-') -> {
                val barcode = item.split("-")[0]
                val quantity = item.split("-")[1].toInt()
                if (purchasedGroupedBarcodes.containsKey(barcode)) {
                    var totalCount = purchasedGroupedBarcodes[barcode] ?: 0
                    purchasedGroupedBarcodes[barcode] = totalCount + quantity
                } else {
                    purchasedGroupedBarcodes[barcode] = quantity
                }
            }
            else -> {
                if (purchasedGroupedBarcodes.containsKey(item)) {
                    var count = purchasedGroupedBarcodes[item] ?: 0
                    purchasedGroupedBarcodes[item] = count + 1
                } else {
                    purchasedGroupedBarcodes[item] = 1
                }
            }
        }
    }

    return purchasedGroupedBarcodes
}

const val expectedReceipt = """
***<没钱赚商店>收据***
名称：雪碧，数量：5瓶，单价：3.00(元)，小计：12.0(元)
名称：荔枝，数量：2斤，单价：15.00(元)，小计：30.0(元)
名称：方便面，数量：3袋，单价：4.50(元)，小计：9.0(元)
----------------------
总计：51.00(元)
节省：7.50(元)
**********************
"""
