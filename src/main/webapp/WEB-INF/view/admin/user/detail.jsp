<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>information user ${id}</title>
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
                                    <h3 class="text-danger">HIỂN THỊ NỘI DUNG</h3>
        
                                </div>
                                <hr />
                                <div class="card" style="width: 60%;">
                                    <div class="card-header bg-primary text-white">
                                        Thông tin người dùng ${id}
                                    </div>
                                    <ul class="list-group list-group-flush">
                                        <li class="list-group-item">ID: ${user.id}</li>
                                        <li class="list-group-item">Full Name: ${user.fullName}</li>
                                        <li class="list-group-item">Email: ${user.email}</li>
                                        <li class="list-group-item">Password: ${user.password}</li>
                                        <li class="list-group-item">Phone: ${user.phone}</li>
                                        <li class="list-group-item">Address: ${user.address}</li>
                                        <li class="list-group-item">Role: ${user.role.name}</li>
                                        <li class="list-group-item">Avatar:<img src="images/avatar/${user.avatar} " height="50" width="50"></li>
                                    </ul>
                                </div>
                                <a href="http://localhost:8080/admin/user/" class="btn btn-success mt-3">Quay lại</a>
        
                            </div>
                        </div>
                    </div>
                </main>
            </div>
             </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"
             crossorigin="anonymous"></script>
         <script src="js/scripts.js"></script>
        </body>

        </html>