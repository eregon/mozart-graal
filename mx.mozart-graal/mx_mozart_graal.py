import mx
import mx_sdk

_suite = mx.suite("mozart-graal")

mx_sdk.register_graalvm_component(mx_sdk.GraalVmLanguage(
    suite=_suite,
    name="Mozart-Graal",
    short_name="moz",
    dir_name="oz",
    license_files=["LICENSE_MOZART_GRAAL.txt"],
    third_party_license_files=[],
    truffle_jars=[
        "mozart-graal:MOZART_GRAAL",
    ],
    support_distributions=[
        "mozart-graal:MOZART_GRAAL_GRAALVM_SUPPORT",
    ],
    provided_executables=[],
    launcher_configs=[
        mx_sdk.LanguageLauncherConfig(
            destination="bin/<exe:oz>",
            jar_distributions=[
                "mozart-graal:MOZART_GRAAL_LAUNCHER",
            ],
            main_class="org.mozartoz.truffle.OzLauncher",
            build_args=["--language:oz"],
            links=[],
        )
    ]
))
