plugins {
    alias(libs.plugins.android.asset.pack)
}

assetPack {
    packName = "media"
    dynamicDelivery {
        deliveryType = "install-time"
    }
}
