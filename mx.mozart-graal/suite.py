suite = {
    "name": "mozart-graal",
    "mxversion": "5.190.8",

    "imports": {
        "suites": [
            {
                "name": "tools",
                "subdir": True,
                "version": "14ac24ff618a6840d9917ae953be1f216bb6cacb",
                "urls": [
                    {"url": "https://github.com/eregon/graal.git", "kind": "git"},
                ],
            },
        ],
    },

    "libraries": {
        "BOOTCOMPILER": {
            "path": "bootcompiler/target/scala-2.11/bootcompiler-assembly-2.0-SNAPSHOT.jar",
            "sha1": "NOCHECK",
        },

        "KRYO": {
            "maven": {
                "groupId": "com.esotericsoftware",
                "artifactId": "kryo",
                "version": "3.0.3"
            },
            "sha1": "01ebca99f633ef31484176a727093e78c7fa43e7",
            "sourceSha1": "a636c50d4c5db89b0ba5dfb4d408e4895293c134",
        },

        "REFLECTASM": {
            "maven": {
                "groupId": "com.esotericsoftware",
                "artifactId": "reflectasm",
                "version": "1.10.1"
            },
            "sha1": "515402cd542ab967a1b5028b4dee1e1800db205b",
            "sourceSha1": "50a86c1d28691acea0e5473ace066c344469c86d",
        },

        "ASM": {
            "maven": {
                "groupId": "org.ow2.asm",
                "artifactId": "asm",
                "version": "5.0.3"
            },
            "sha1": "dcc2193db20e19e1feca8b1240dbbc4e190824fa",
        },

        "MINLOG": {
            "maven": {
                "groupId": "com.esotericsoftware",
                "artifactId": "minlog",
                "version": "1.3.0"
            },
            "sha1": "ff07b5f1b01d2f92bb00a337f9a94873712f0827",
            "sourceSha1": "9ee9f7fe99d5acfcb974e28acbad9349fb33a765",
        },

        "OBJENESIS": {
            "maven": {
                "groupId": "org.objenesis",
                "artifactId": "objenesis",
                "version": "2.1"
            },
            "sha1": "87c0ea803b69252868d09308b4618f766f135a96",
            "sourceSha1": "0611a57d836e2c320d59f9851d4ad587f3c8472e",
        },
    },

    "projects": {
        "org.mozartoz.truffle": {
            "dir": "vm",
            "sourceDirs": ["src"],
            "dependencies": [
                "BOOTCOMPILER",
                "truffle:TRUFFLE_API",
                "KRYO",
                "REFLECTASM",
                "ASM",
                "MINLOG",
                "OBJENESIS",
            ],
            "annotationProcessors": [
                "truffle:TRUFFLE_DSL_PROCESSOR",
            ],
            "javaCompliance": "1.8",
        },

        "org.mozartoz.truffle.launcher": {
            "dir": "vm",
            "sourceDirs": ["launcher"],
            "dependencies": [
                "sdk:LAUNCHER_COMMON",
            ],
            "javaCompliance": "1.8",
        },
    },

    "distributions": {
        "MOZART_GRAAL": {
            "dependencies": [
                "org.mozartoz.truffle",
            ],
            "distDependencies": [
                "truffle:TRUFFLE_API",
            ],
        },

        "MOZART_GRAAL_LAUNCHER": {
            "dependencies": [
                "org.mozartoz.truffle.launcher",
            ],
            "distDependencies": [
                "sdk:LAUNCHER_COMMON",
            ],
        },

        "MOZART_GRAAL_GRAALVM_SUPPORT": {
            "native": True,
            "layout": {
                "./": [
                    "file:README.md",
                    "file:mx.mozart-graal/native-image.properties",
                    "file:examples",
                    "file:lib",
                    "file:platform-test",
                ],
                "wish/": [
                    "file:wish/ozwish",
                ],
                "LICENSE_MOZART_GRAAL.txt": "file:LICENSE.txt",
            },
        },
    },
}
