/*
 * Copyright (C) 2019 Bosch Software Innovations GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */

package org.ossreviewtoolkit.spdx

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.WordSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class SpdxDeclaredLicenseMappingTest : WordSpec({
    "The list" should {
        "not contain any duplicate keys with respect to capitalization" {
            val keys = SpdxDeclaredLicenseMapping.mappingList.unzip().first.toMutableList()
            val uniqueKeys = SpdxDeclaredLicenseMapping.mapping.keys

            // Remove keys one by one as calling "-" would remove all occurrences of a key.
            uniqueKeys.forEach { uniqueKey -> keys.remove(uniqueKey) }

            keys should beEmpty()
        }
    }

    "The mapping" should {
        "not contain single ID strings" {
            val licenseIdMapping = SpdxDeclaredLicenseMapping.mapping.filter { (_, expression) ->
                expression is SpdxLicenseIdExpression
            }

            licenseIdMapping.keys.forAll { declaredLicense ->
                try {
                    val tokens = getTokensByTypeForExpression(declaredLicense)

                    tokens.size shouldBeGreaterThanOrEqual 2

                    if (tokens.size == 2) {
                        // Rule out that the 2 tokens are caused by IDSTRING and PLUS.
                        declaredLicense shouldContain " "
                    }
                } catch (e: SpdxException) {
                    // For untokenizable strings no further checks are needed.
                }
            }
        }

        "not contain plain SPDX license ids" {
            assertSoftly {
                SpdxDeclaredLicenseMapping.mapping.forEach { (declaredLicense, _) ->
                    "\"$declaredLicense\" maps to ${SpdxLicense.forId(declaredLicense)}" shouldBe
                            "\"$declaredLicense\" maps to null"
                }
            }
        }

        "be case-insensitve" {
            val map = SpdxDeclaredLicenseMapping.mapping
            map.forEach { (key, license) ->
                map[key.toLowerCase()] shouldBe license
                map[key.toUpperCase()] shouldBe license
                map[key.toLowerCase().capitalize()] shouldBe license
            }
        }
    }
})
