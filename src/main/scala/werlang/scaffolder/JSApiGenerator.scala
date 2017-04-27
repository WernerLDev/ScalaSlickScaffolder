package werlang.scaffolder
import werlang.scaffolder._

case class JSApiGenerator(all:List[SpecEntity]) {

    val tpl:String = """
                    |var csrf = document.getElementById("csrftoken").innerText;
                    |
                    |function handleErrors(response) {
                    |    if(!response.ok) {
                    |        alert(response.status + " - " + response.statusText);
                    |        throw Error(response.statusText);
                    |    } else {
                    |        return response;
                    |    }
                    |}
                    |
                    |function ApiCall(call, method, body, contenttype) {
                    |    var headers = {
                    |            "Csrf-Token": csrf
                    |    }
                    |    if(contenttype != false && contenttype != null){
                    |        headers["Content-Type"] = contenttype;
                    |    } else if(contenttype == null) {
                    |        headers["Content-Type"] = "application/json";
                    |    }
                    |    var params = {
                    |        method: method,
                    |        credentials: 'include',
                    |        headers: headers 
                    |    }
                    |    if(method != "GET" && method != "HEAD") {
                    |        params["body"] = body;
                    |    } 
                    |
                    |    return fetch(call, params).then(handleErrors).then(r => r.json());
                    |}
                    |
                    |{apicalls}
                    |""".stripMargin


    val apicalltpl = """|export function {apiname}({params}) {
                    |    return ApiCall({apicall});
                    |}
                    |
                    """.stripMargin

    def generate = {
        
    }

}