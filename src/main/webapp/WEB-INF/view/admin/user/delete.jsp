<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@taglib uri="http://www.springframework.org/tags/form" prefix="form" %>


            <html lang="en">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Delete User</title>
                <!-- Latest compiled and minified CSS -->
                <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">

                <!-- Latest compiled JavaScript -->
                <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>

                <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js"></script>
                <link rel="stylesheet" href="/css/styles.css" />

            </head>

            <body class="sb-nav-fixed">
                <jsp:include page="../layout/header.jsp"/>  

                <div id="layoutSidenav">
                    <jsp:include page="../layout/sidebar.jsp"/>
                    <div id="layoutSidenav_content">
                        <main>
                            <div class="container mt-5">
                                <div class="row">
                                    <div class="col-md-6 col-12 mx-auto">
                                        <h1>Delete user {id}</h1>
                                        <hr />
                                        <div class="alert alert-warning" role="alert">
                                            Bạn có chắc muốn xóa?
                                          </div>
                                    </div>
                                    <form:form method="post" action="/admin/user/delete/" modelAttribute="deleteUser">
                                        <div class="mb-3" style="display: none;">
                                            <label for="exampleInputId1" class="form-label">Id:</label>
                                            <form:input path="id" type="number" class="form-control" />
                                        </div>
                                        <button class="btn-primary">Xác nhận</button>
                                    </form:form>
                                </div>
                        </main>
                    </div>
                </div>
               
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"
            crossorigin="anonymous"></script>
        <script src="js/scripts.js"></script>
            </body>

            </html>