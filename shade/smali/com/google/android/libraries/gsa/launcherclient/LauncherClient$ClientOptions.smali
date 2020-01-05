.class public Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$ClientOptions;
.super Ljava/lang/Object;
.source "LauncherClient.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/google/android/libraries/gsa/launcherclient/LauncherClient;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x9
    name = "ClientOptions"
.end annotation


# instance fields
.field private final a:I


# direct methods
.method public constructor <init>(ZZZ)V
    .locals 1

    .line 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    const/4 v0, 0x0

    or-int/2addr p1, v0

    if-eqz p2, :cond_0

    const/4 p2, 0x2

    goto :goto_0

    :cond_0
    move p2, v0

    :goto_0
    or-int/2addr p1, p2

    if-eqz p3, :cond_1

    const/4 v0, 0x4

    :cond_1
    or-int/2addr p1, v0

    .line 5
    iput p1, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$ClientOptions;->a:I

    return-void
.end method

.method static synthetic a(Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$ClientOptions;)I
    .locals 0

    .line 7
    iget p0, p0, Lcom/google/android/libraries/gsa/launcherclient/LauncherClient$ClientOptions;->a:I

    return p0
.end method
