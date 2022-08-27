package com.melody.opengl.camerax.model

import com.melody.opengl.camerax.filters.gpuFilters.baseFilter.*
import com.melody.opengl.camerax.filters.gpuFilters.paramsFilter.*

class MagicFilterFactory {
    companion object {
        var currentFilterType = MagicFilterType.NONE
            private set

        fun initFilters(type: MagicFilterType?): GPUImageFilter? {
            if (type == null) {
                return null
            }
            currentFilterType = type
            return when (type) {
                MagicFilterType.WHITECAT -> MagicWhiteCatFilter()
                MagicFilterType.BLACKCAT -> MagicBlackCatFilter()
                MagicFilterType.SKINWHITEN -> MagicSkinWhitenFilter()
                MagicFilterType.ROMANCE -> MagicRomanceFilter()
                MagicFilterType.SAKURA -> MagicSakuraFilter()
                MagicFilterType.AMARO -> MagicAmaroFilter()
                MagicFilterType.WALDEN -> MagicWaldenFilter()
                MagicFilterType.ANTIQUE -> MagicAntiqueFilter()
                MagicFilterType.CALM -> MagicCalmFilter()
                MagicFilterType.BRANNAN -> MagicBrannanFilter()
                MagicFilterType.BROOKLYN -> MagicBrooklynFilter()
                MagicFilterType.EARLYBIRD -> MagicEarlyBirdFilter()
                MagicFilterType.FREUD -> MagicFreudFilter()
                MagicFilterType.HEFE -> MagicHefeFilter()
                MagicFilterType.HUDSON -> MagicHudsonFilter()
                MagicFilterType.INKWELL -> MagicInkwellFilter()
                MagicFilterType.KEVIN -> MagicKevinFilter()
                MagicFilterType.N1977 -> MagicN1977Filter()
                MagicFilterType.NASHVILLE -> MagicNashvilleFilter()
                MagicFilterType.PIXAR -> MagicPixarFilter()
                MagicFilterType.RISE -> MagicRiseFilter()
                MagicFilterType.SIERRA -> MagicSierraFilter()
                MagicFilterType.SUTRO -> MagicSutroFilter()
                MagicFilterType.TOASTER2 -> MagicToasterFilter()
                MagicFilterType.VALENCIA -> MagicValenciaFilter()
                MagicFilterType.XPROII -> MagicXproIIFilter()
                MagicFilterType.EVERGREEN -> MagicEvergreenFilter()
                MagicFilterType.HEALTHY -> MagicHealthyFilter()
                MagicFilterType.COOL -> MagicCoolFilter()
                MagicFilterType.EMERALD -> MagicEmeraldFilter()
                MagicFilterType.LATTE -> MagicLatteFilter()
                MagicFilterType.WARM -> MagicWarmFilter()
                MagicFilterType.TENDER -> MagicTenderFilter()
                MagicFilterType.SWEETS -> MagicSweetsFilter()
                MagicFilterType.FAIRYTALE -> MagicFairytaleFilter()
                MagicFilterType.SUNRISE -> MagicSunriseFilter()
                MagicFilterType.SUNSET -> MagicSunsetFilter()
                MagicFilterType.BRIGHTNESS -> GPUImageBrightnessFilter()
                MagicFilterType.CONTRAST -> GPUImageContrastFilter()
                MagicFilterType.EXPOSURE -> GPUImageExposureFilter()
                MagicFilterType.HUE -> GPUImageHueFilter()
                MagicFilterType.SATURATION -> GPUImageSaturationFilter()
                MagicFilterType.SHARPEN -> GPUImageSharpenFilter()
                else -> null
            }
        }
    }
}