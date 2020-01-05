.class public final Lcom/google/android/libraries/a/c;
.super Lcom/google/android/a/a;
.source "ILauncherOverlay.java"

# interfaces
.implements Lcom/google/android/libraries/a/a;


# direct methods
.method constructor <init>(Landroid/os/IBinder;)V
    .locals 1

    const-string v0, "com.google.android.libraries.launcherclient.ILauncherOverlay"

    .line 1
    invoke-direct {p0, p1, v0}, Lcom/google/android/a/a;-><init>(Landroid/os/IBinder;Ljava/lang/String;)V

    return-void
.end method


# virtual methods
.method public final a(F)V
    .locals 1
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    .line 6
    invoke-virtual {p0}, Lcom/google/android/libraries/a/c;->a()Landroid/os/Parcel;

    move-result-object v0

    .line 7
    invoke-virtual {v0, p1}, Landroid/os/Parcel;->writeFloat(F)V

    const/4 p1, 0x2

    .line 8
    invoke-virtual {p0, p1, v0}, Lcom/google/android/libraries/a/c;->b(ILandroid/os/Parcel;)V

    return-void
.end method

.method public final a(I)V
    .locals 1
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    .line 28
    invoke-virtual {p0}, Lcom/google/android/libraries/a/c;->a()Landroid/os/Parcel;

    move-result-object v0

    .line 29
    invoke-virtual {v0, p1}, Landroid/os/Parcel;->writeInt(I)V

    const/4 p1, 0x6

    .line 30
    invoke-virtual {p0, p1, v0}, Lcom/google/android/libraries/a/c;->b(ILandroid/os/Parcel;)V

    return-void
.end method

.method public final a(Landroid/os/Bundle;Lcom/google/android/libraries/a/d;)V
    .locals 1
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    .line 19
    invoke-virtual {p0}, Lcom/google/android/libraries/a/c;->a()Landroid/os/Parcel;

    move-result-object v0

    .line 20
    invoke-static {v0, p1}, Lcom/google/android/a/c;->a(Landroid/os/Parcel;Landroid/os/Parcelable;)V

    .line 21
    invoke-static {v0, p2}, Lcom/google/android/a/c;->a(Landroid/os/Parcel;Landroid/os/IInterface;)V

    const/16 p1, 0xe

    .line 22
    invoke-virtual {p0, p1, v0}, Lcom/google/android/libraries/a/c;->b(ILandroid/os/Parcel;)V

    return-void
.end method

.method public final a(Landroid/view/WindowManager$LayoutParams;Lcom/google/android/libraries/a/d;I)V
    .locals 1
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    .line 13
    invoke-virtual {p0}, Lcom/google/android/libraries/a/c;->a()Landroid/os/Parcel;

    move-result-object v0

    .line 14
    invoke-static {v0, p1}, Lcom/google/android/a/c;->a(Landroid/os/Parcel;Landroid/os/Parcelable;)V

    .line 15
    invoke-static {v0, p2}, Lcom/google/android/a/c;->a(Landroid/os/Parcel;Landroid/os/IInterface;)V

    .line 16
    invoke-virtual {v0, p3}, Landroid/os/Parcel;->writeInt(I)V

    const/4 p1, 0x4

    .line 17
    invoke-virtual {p0, p1, v0}, Lcom/google/android/libraries/a/c;->b(ILandroid/os/Parcel;)V

    return-void
.end method

.method public final a(Z)V
    .locals 1
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    .line 24
    invoke-virtual {p0}, Lcom/google/android/libraries/a/c;->a()Landroid/os/Parcel;

    move-result-object v0

    .line 25
    invoke-static {v0, p1}, Lcom/google/android/a/c;->a(Landroid/os/Parcel;Z)V

    const/4 p1, 0x5

    .line 26
    invoke-virtual {p0, p1, v0}, Lcom/google/android/libraries/a/c;->b(ILandroid/os/Parcel;)V

    return-void
.end method

.method public final a_()V
    .locals 2
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    .line 3
    invoke-virtual {p0}, Lcom/google/android/libraries/a/c;->a()Landroid/os/Parcel;

    move-result-object v0

    const/4 v1, 0x1

    .line 4
    invoke-virtual {p0, v1, v0}, Lcom/google/android/libraries/a/c;->b(ILandroid/os/Parcel;)V

    return-void
.end method

.method public final b()V
    .locals 2
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    .line 10
    invoke-virtual {p0}, Lcom/google/android/libraries/a/c;->a()Landroid/os/Parcel;

    move-result-object v0

    const/4 v1, 0x3

    .line 11
    invoke-virtual {p0, v1, v0}, Lcom/google/android/libraries/a/c;->b(ILandroid/os/Parcel;)V

    return-void
.end method

.method public final b(I)V
    .locals 1
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    .line 38
    invoke-virtual {p0}, Lcom/google/android/libraries/a/c;->a()Landroid/os/Parcel;

    move-result-object v0

    .line 39
    invoke-virtual {v0, p1}, Landroid/os/Parcel;->writeInt(I)V

    const/16 p1, 0x10

    .line 40
    invoke-virtual {p0, p1, v0}, Lcom/google/android/libraries/a/c;->b(ILandroid/os/Parcel;)V

    return-void
.end method

.method public final b(Z)V
    .locals 1
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    .line 46
    invoke-virtual {p0}, Lcom/google/android/libraries/a/c;->a()Landroid/os/Parcel;

    move-result-object v0

    .line 47
    invoke-static {v0, p1}, Lcom/google/android/a/c;->a(Landroid/os/Parcel;Z)V

    const/16 p1, 0xa

    .line 48
    invoke-virtual {p0, p1, v0}, Lcom/google/android/libraries/a/c;->b(ILandroid/os/Parcel;)V

    return-void
.end method

.method public final c()V
    .locals 2
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    .line 32
    invoke-virtual {p0}, Lcom/google/android/libraries/a/c;->a()Landroid/os/Parcel;

    move-result-object v0

    const/4 v1, 0x7

    .line 33
    invoke-virtual {p0, v1, v0}, Lcom/google/android/libraries/a/c;->b(ILandroid/os/Parcel;)V

    return-void
.end method

.method public final c(I)V
    .locals 1
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    .line 42
    invoke-virtual {p0}, Lcom/google/android/libraries/a/c;->a()Landroid/os/Parcel;

    move-result-object v0

    .line 43
    invoke-virtual {v0, p1}, Landroid/os/Parcel;->writeInt(I)V

    const/16 p1, 0x9

    .line 44
    invoke-virtual {p0, p1, v0}, Lcom/google/android/libraries/a/c;->b(ILandroid/os/Parcel;)V

    return-void
.end method

.method public final d()V
    .locals 2
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    .line 35
    invoke-virtual {p0}, Lcom/google/android/libraries/a/c;->a()Landroid/os/Parcel;

    move-result-object v0

    const/16 v1, 0x8

    .line 36
    invoke-virtual {p0, v1, v0}, Lcom/google/android/libraries/a/c;->b(ILandroid/os/Parcel;)V

    return-void
.end method

.method public final e()Z
    .locals 2
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    .line 50
    invoke-virtual {p0}, Lcom/google/android/libraries/a/c;->a()Landroid/os/Parcel;

    move-result-object v0

    const/16 v1, 0xc

    .line 51
    invoke-virtual {p0, v1, v0}, Lcom/google/android/libraries/a/c;->a(ILandroid/os/Parcel;)Landroid/os/Parcel;

    move-result-object p0

    .line 52
    invoke-static {p0}, Lcom/google/android/a/c;->a(Landroid/os/Parcel;)Z

    move-result v0

    .line 53
    invoke-virtual {p0}, Landroid/os/Parcel;->recycle()V

    return v0
.end method

.method public final f()Z
    .locals 2
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    .line 55
    invoke-virtual {p0}, Lcom/google/android/libraries/a/c;->a()Landroid/os/Parcel;

    move-result-object v0

    const/16 v1, 0xd

    .line 56
    invoke-virtual {p0, v1, v0}, Lcom/google/android/libraries/a/c;->a(ILandroid/os/Parcel;)Landroid/os/Parcel;

    move-result-object p0

    .line 57
    invoke-static {p0}, Lcom/google/android/a/c;->a(Landroid/os/Parcel;)Z

    move-result v0

    .line 58
    invoke-virtual {p0}, Landroid/os/Parcel;->recycle()V

    return v0
.end method
