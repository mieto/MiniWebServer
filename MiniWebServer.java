package jdbc_first;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


public class MiniWebServer {

	public static void main(String[] args) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
		server.createContext("/meibo", new MeiboHandler());
		server.setExecutor(null);
		server.start();
		System.out.println("サーバを起動! ポート番号8080にてアクセス!");
	}

}

//getDatabaseDataによって文字列型でデータ内容を取得している MeiboHandlerで読み込んでHttpServerに渡す
class MeiboHandler implements HttpHandler {
	@Override
	public void handle(HttpExchange exchange) throws IOException{
		System.out.println("リクエストを受信");
		String response = getDatabaseData();
		//System.out.println("responseの中身: " + response);
		//htmlタグである事を明示
		exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
		exchange.sendResponseHeaders(200, response.getBytes("UTF-8").length);
		OutputStream os = exchange.getResponseBody();
		os.write(response.getBytes("UTF-8"));
		os.close();
	}
	
	private String getDatabaseData() {
		//sbを表示させるappendする形で追記していく必要がある
		StringBuilder sb = new StringBuilder();
		String url = "jdbc:sqlite:/適宜変更 path sql database/";
		
		sb.append("<html><body>");
		sb.append("<h2>名簿リスト</h2>");
		sb.append("<table border='1'>");
		sb.append("<tr><th>ID</th><th>Name</th><th>Detail></th></tr>");
		
		try(Connection conn = DriverManager.getConnection(url);
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM meibo")){
			while(rs.next()) {
				int id = rs.getInt("id");
				String name = rs.getString("name");
				String detail = rs.getString("detail");
				
				sb.append("<tr>")
				.append("<td>").append(id).append("</td>")
				.append("<td>").append(name).append("</td>")
				.append("<td>").append(detail).append("</td>")
				.append("</tr>");
			}
		}catch(SQLException e) {
			sb.append("<p>DB ERROR: ").append(e.getMessage()).append("</p>");
		}
		sb.append("</table>");
		sb.append("</body></html>");
		
		return sb.toString();
	}
}
