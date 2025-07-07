<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>List User</title>
            <!-- Latest compiled and minified CSS -->
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">

            <!-- Latest compiled JavaScript -->
            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>

            <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js"></script>

            <link href="/css/styles.css" rel="stylesheet" />

        </head>

        <body class="sb-nav-fixed">
            <jsp:include page="../layout/header.jsp" />
            <div id="layoutSidenav">
                <jsp:include page="../layout/sidebar.jsp" />
                <div id="layoutSidenav_content">
                    <main>
                        <div class="container mt-5">
                            <div class="row">
                                <div class="col-md-12">
                                    <div class="d-flex justify-content-between">
                                        <h3 class="text-danger">Danh sách tài khoản</h3>
                                        <a href="/admin/user/create" class="btn btn-primary">Thêm mới</a>
            
                                    </div>
                                    <hr>
                                    <table class=" table table-bordered table-striped table-hover mt-5">
                                        <thead>
                                            <tr class="table-success">
                                                <th>id</th>
                                                <th>fullName</th>
                                                <th>role</th>
                                                <th>action</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="user" items="${users}">
                                                <tr>
                                                    <td>${user.id}</td>
                                                    <td>${user.fullName}</td>
                                                    <td>${user.role.name}</td>
                                                    <td><a href="/admin/user/view/${user.id}" class="btn btn-success">View</a>
                                                        <a href="/admin/user/delete/${user.id}" class="btn btn-danger">Delete</a>
                                                        <a href="/admin/user/update/${user.id}" class="btn btn-warning">update</a>
                                                    </td>
            
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
            
                    </main>
                    <jsp:include page="../layout/footer.jsp" />
                </div>
            </div>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"
            crossorigin="anonymous"></script>
        <script src="js/scripts.js"></script>
            
        </body>

        </html>