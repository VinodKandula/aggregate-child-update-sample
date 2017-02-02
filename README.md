Spring Data REST issue repro project
======

Composite key child update issue
------
See `AggregateCompositeKeyUpdateTests`, which demonstrates the issue in integration tests.

You can create a `Post` with a `Comment` but when updating an existing `Post` to add a comment, 
the reference to the `Post` is not available to the `Comment` being added.

The bidirectional relationship is not desired other than for the composite key 
(implemented this way due to the prior database design). If there is a way to make 
this unidirectional while maintaining the composite key including the FK, it seems
the issue with back-reference to `Post` can be eliminated.

Asked on StackOverflow: https://stackoverflow.com/questions/41999835/spring-data-rest-jpa-update-onetomany-collection-with-composite-key

### Manual reproduction

After starting the application, using [HTTPie](https://httpie.org/):

1) Create a `Post` without any comments

```
$ http POST :8080/posts title="My first post" -v

POST /posts HTTP/1.1
Accept: application/json, */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Content-Length: 26
Content-Type: application/json
Host: localhost:8080
User-Agent: HTTPie/0.9.8

{
    "title": "My first post"
}

HTTP/1.1 201
Content-Type: application/json;charset=UTF-8
Date: Thu, 02 Feb 2017 10:14:16 GMT
Location: http://localhost:8080/posts/1
Transfer-Encoding: chunked
X-Application-Context: application

{
    "_links": {
        "post": {
            "href": "http://localhost:8080/posts/1"
        },
        "self": {
            "href": "http://localhost:8080/posts/1"
        }
    },
    "comments": [],
    "title": "My first post"
}
```

2) Add a `Comment` to that `Post` via `PUT`

```
$ http PUT :8080/posts/1 title="My first post" comments:='[{"id":1,"content":"hi"}]' -v

PUT /posts/1 HTTP/1.1
Accept: application/json, */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Content-Length: 68
Content-Type: application/json
Host: localhost:8080
User-Agent: HTTPie/0.9.8

{
    "comments": [
        {
            "content": "hi",
            "id": 1
        }
    ],
    "title": "My first post"
}

HTTP/1.1 409
Content-Type: application/json;charset=UTF-8
Date: Thu, 02 Feb 2017 10:14:33 GMT
Transfer-Encoding: chunked
X-Application-Context: application

{
    "cause": {
        "cause": {
            "cause": null,
            "message": "NULL not allowed for column \"POST_ID\"; SQL statement:\ninsert into comment (content, id, post_id) values (?, ?, ?) [23502-193]"
        },
        "message": "could not execute statement"
    },
    "message": "could not execute statement; SQL [n/a]; constraint [null]; nested exception is org.hibernate.exception.ConstraintViolationException: could not execute statement"
}
```

JSON Patch issues
------

JSON Patch ([RFC6902](https://tools.ietf.org/html/rfc6902)) requests to add to a collection of entities 
under an aggregate root are failing with Hopper and Ingalls but working with Gosling.
 
This can be tested by switching the Spring Boot parent version used in the POM and following the steps below.

| Spring Boot | Spring Data | JSON Patch? |
| ----------- | ----------- | ----------- |
|    1.3.x    |   Gosling   |    Works    |
|    1.4.x    |    Hopper   |    Error    |
|    1.5.x    |   Ingalls   |    Error    |

### Integration test reproduction
Run the single test class `AggregateChildUpdateSampleApplicationTests` to verify the JSON Patch behavior.
Unfortunately, I could not figure out a way to write the web integration test so that it compiles on all 
versions of Spring Boot from 1.3.x to 1.5.x due to deprecations and removals of test annotations.

Therefore, some work of uncommenting and commenting annotations and imports is needed when switching between
Spring Boot versions. The committed version runs with Spring Boot 1.5.x. 
To switch to Spring Boot 1.3.x compatible version, uncomment the commented portions and comment the 
`@SpringBootTest` annotation and import.

### Manual reproduction

Example requests shown using [HTTPie](https://httpie.org/).

1. Create a new `Post`

```
$ http POST :8080/posts title="My first post" -v
POST /posts HTTP/1.1
Accept: application/json, */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Content-Length: 26
Content-Type: application/json
Host: localhost:8080
User-Agent: HTTPie/0.9.8

{
    "title": "My first post"
}

HTTP/1.1 201 Created
Content-Type: application/json;charset=UTF-8
Date: Wed, 01 Feb 2017 11:47:33 GMT
Location: http://localhost:8080/posts/1
Server: Apache-Coyote/1.1
Transfer-Encoding: chunked
X-Application-Context: application

{
    "_links": {
        "post": {
            "href": "http://localhost:8080/posts/1"
        },
        "self": {
            "href": "http://localhost:8080/posts/1"
        }
    },
    "comments": [],
    "title": "My first post"
}
```

2. Add a `Comment` with JSON Patch

```
$ echo '[{"op":"add", "path":"/comments/-", "value":{"content": "Cool post"}}]' | http PATCH :8080/posts/1 Content-Type:application/json-patch+json -v
PATCH /posts/1 HTTP/1.1
Accept: application/json, */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Content-Length: 71
Content-Type: application/json-patch+json
Host: localhost:8080
User-Agent: HTTPie/0.9.8

[
    {
        "op": "add",
        "path": "/comments/-",
        "value": {
            "content": "Cool post"
        }
    }
]

HTTP/1.1 200 OK
Content-Type: application/json;charset=UTF-8
Date: Wed, 01 Feb 2017 11:57:01 GMT
Server: Apache-Coyote/1.1
Transfer-Encoding: chunked
X-Application-Context: application

{
    "_links": {
        "post": {
            "href": "http://localhost:8080/posts/1"
        },
        "self": {
            "href": "http://localhost:8080/posts/1"
        }
    },
    "comments": [
        {
            "content": "Cool post"
        }
    ],
    "title": "My first post"
}
```

With Hopper or Ingalls, the following error occurs instead of 
successfully adding the `Comment` as above (when using Gosling).

```
{
    "cause": {
        "cause": null,
        "message": "Expression [comments.-] @8: EL1049E: Unexpected data after '.': 'minus(-)'"
    },
    "message": "Could not read PATCH operations! Expected application/json-patch+json!; nested exception is org.springframework.expression.spel.SpelParseException: Expression [comments.-] @8: EL1049E: Unexpected data after '.': 'minus(-)'"
}
```

This alludes to a problem with parsing the syntax for adding an element 
to the end of an array using the `-` (minus) index.

However, even if a request to add the entire array is used, the request fails with a different error:

```
$ echo '[{"op":"add", "path":"/comments", "value":[{"content": "Cool post!"}]}]' | http PATCH :8080/posts/1 Content-Type:application/json-patch+json -v
PATCH /posts/1 HTTP/1.1
Accept: application/json, */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Content-Length: 72
Content-Type: application/json-patch+json
Host: localhost:8080
User-Agent: HTTPie/0.9.8

[
    {
        "op": "add",
        "path": "/comments",
        "value": [
            {
                "content": "Cool post!"
            }
        ]
    }
]

HTTP/1.1 400
Connection: close
Content-Type: application/json;charset=UTF-8
Date: Wed, 01 Feb 2017 12:02:33 GMT
Transfer-Encoding: chunked
X-Application-Context: application

{
    "cause": {
        "cause": {
            "cause": null,
            "message": "failed to lazily initialize a collection, could not initialize proxy - no Session"
        },
        "message": "Could not read [{\"content\":\"Cool post!\"}] into class org.hibernate.collection.internal.PersistentSet!"
    },
    "message": "Could not read an object of type class com.example.data.Post from the request!; nested exception is org.springframework.data.rest.webmvc.json.patch.PatchException: Could not read [{\"content\":\"Cool post!\"}] into class org.hibernate.collection.internal.PersistentSet!"
}
```

Related links
-------

* https://stackoverflow.com/questions/34843297/modify-onetomany-entity-in-spring-data-rest-without-its-repository
* https://jira.spring.io/browse/DATAREST-813
