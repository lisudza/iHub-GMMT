/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/stuart/workspace/iHub RC2/src/com/ihub/android/app/service/UpdateMembersInfo.aidl
 */
package com.ihub.android.app.service;
public interface UpdateMembersInfo extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.ihub.android.app.service.UpdateMembersInfo
{
private static final java.lang.String DESCRIPTOR = "com.ihub.android.app.service.UpdateMembersInfo";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.ihub.android.app.service.UpdateMembersInfo interface,
 * generating a proxy if needed.
 */
public static com.ihub.android.app.service.UpdateMembersInfo asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.ihub.android.app.service.UpdateMembersInfo))) {
return ((com.ihub.android.app.service.UpdateMembersInfo)iin);
}
return new com.ihub.android.app.service.UpdateMembersInfo.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_fetchMembers:
{
data.enforceInterface(DESCRIPTOR);
java.util.Map _result = this.fetchMembers();
reply.writeNoException();
reply.writeMap(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.ihub.android.app.service.UpdateMembersInfo
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public java.util.Map fetchMembers() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.util.Map _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_fetchMembers, _data, _reply, 0);
_reply.readException();
java.lang.ClassLoader cl = (java.lang.ClassLoader)this.getClass().getClassLoader();
_result = _reply.readHashMap(cl);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_fetchMembers = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public java.util.Map fetchMembers() throws android.os.RemoteException;
}
