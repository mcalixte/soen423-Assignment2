package StoreApp;

/**
* StoreApp/StoreHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from store.idl
* Tuesday, October 27, 2020 2:10:51 AM EDT
*/

public final class StoreHolder implements org.omg.CORBA.portable.Streamable
{
  public StoreApp.Store value = null;

  public StoreHolder ()
  {
  }

  public StoreHolder (StoreApp.Store initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = StoreApp.StoreHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    StoreApp.StoreHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return StoreApp.StoreHelper.type ();
  }

}
