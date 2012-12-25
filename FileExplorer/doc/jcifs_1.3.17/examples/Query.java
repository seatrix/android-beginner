import jcifs.UniAddress;
import jcifs.netbios.NbtAddress;

public class Query {

    public static void main( String argv[] ) throws Exception {
        UniAddress ua;
        String cn;
        
        argv = new String[]{"192.168.51.44"};
        ua = UniAddress.getByName( argv[0] );

        cn = ua.firstCalledName();
        do {
            System.out.println( "calledName=" + cn );
        } while(( cn = ua.nextCalledName() ) != null && !cn.startsWith(NbtAddress.SMBSERVER_NAME));
    }
}
