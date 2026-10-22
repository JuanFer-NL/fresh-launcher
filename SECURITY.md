# Security policy

## Reporting a vulnerability

Please **do not open a public issue** for security problems.

Report them privately through GitHub: go to the
[Security tab](https://github.com/JuanFer-NL/fresh-launcher/security/advisories/new)
and choose **Report a vulnerability**. You'll get a reply as soon as possible, and the
fix will be credited to you unless you prefer otherwise.

## Supported versions

Only the [latest release](https://github.com/JuanFer-NL/fresh-launcher/releases/latest)
receives security fixes.

## Verifying a release

Official APKs are signed with this certificate (SHA-256):

```
0f43339248a304af4e76fdd012df244be883c896ff07a7f111f523bd2da686a1
```

Check it with `apksigner verify --print-certs fresh-launcher-X.Y.Z.apk`. An APK signed with
any other certificate was not built by this project.

---

**En español:** no abras un issue público por problemas de seguridad. Reportalos en privado desde
la pestaña *Security → Report a vulnerability* del repositorio.
