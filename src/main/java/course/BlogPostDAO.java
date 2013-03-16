/*
 * Copyright (c) 2008 - 2013 10gen, Inc. <http://10gen.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package course;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.sun.org.apache.bcel.internal.generic.ACONST_NULL;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BlogPostDAO {
    DBCollection postsCollection;

    public BlogPostDAO(final DB blogDatabase) {
        postsCollection = blogDatabase.getCollection("posts");
    }

    // Return a single post corresponding to a permalink
    public DBObject findByPermalink(String permalink) {
        System.out.println("fetching single blog entry " + permalink);

        DBObject post = null;

        BasicDBObject filter = new BasicDBObject("permalink", permalink);
        post = postsCollection.findOne(filter);

        return post;
    }

    // Return a list of posts in descending order. Limit determines
    // how many posts are returned.
    public List<DBObject> findByDateDescending(int limit) {
        System.out.println("fetching blog entries");

        List<DBObject> posts = null;

        DBObject sort = new BasicDBObject("date", -1);
        posts = postsCollection.find().sort(sort).limit(limit).toArray();

        return posts;
    }


    public String addPost(String title, String body, List tags, String username) {

        System.out.println("inserting blog entry " + title + " " + body);

        String permalink = title.replaceAll("\\s", "_"); // whitespace becomes _
        permalink = permalink.replaceAll("\\W", ""); // get rid of non alphanumeric
        permalink = permalink.toLowerCase();

        BasicDBObject post = new BasicDBObject();
        post = post.append("author", username);
        post.append("title", title);
        post.append("body", body);
        post.append("permalink", permalink);
        post.append("tags", tags);
        post.append("comments", new ArrayList());
        post.append("date", new Date());

        postsCollection.insert(post);

        return permalink;
    }


    // White space to protect the innocent


    // Append a comment to a blog post
    public void addPostComment(final String name, final String email, final String body,
                               final String permalink) {
        BasicDBObject filter = new BasicDBObject("permalink", permalink);

        BasicDBObject commentRecord = new BasicDBObject("body", body);
        commentRecord.append("author", name);
        if (email != null && !email.contentEquals("")) {
            commentRecord.append("email", email);
        }

        DBObject commentsRecord = new BasicDBObject("comments", commentRecord);
        DBObject pushCommand = new BasicDBObject("$push", commentsRecord);

        postsCollection.update(filter, pushCommand);

    }


}
