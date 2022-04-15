const { MongoClient, ServerApiVersion } = require('mongodb');
const uri = "mongodb+srv://abuser:OLb1hZPcnBK4bJEr@abprojekt.4qafu.mongodb.net/test6?retryWrites=true&w=majority";
const client = new MongoClient(uri, { useNewUrlParser: true, useUnifiedTopology: true, serverApi: ServerApiVersion.v1 });


const uri2 = "mongodb+srv://abuser:Akhnjofxy5QEoF8P@indexcluster.niofn.mongodb.net/myFirstDatabase?retryWrites=true&w=majority";
const indexClient = new MongoClient(uri2, { useNewUrlParser: true, useUnifiedTopology: true, serverApi: ServerApiVersion.v1 });

async function valami(){
    await indexClient.connect();
    let hello = await indexClient.db('beszuras').collection('btabla.pk').findOne({_id: 1});
    console.log(hello);
    indexClient.close();
}
valami();