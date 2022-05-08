const { MongoClient, ServerApiVersion } = require('mongodb');
const uri = "mongodb+srv://abuser:OLb1hZPcnBK4bJEr@abprojekt.4qafu.mongodb.net/test6?retryWrites=true&w=majority";
const client = new MongoClient(uri, { useNewUrlParser: true, useUnifiedTopology: true, serverApi: ServerApiVersion.v1 });


const uri2 = "mongodb+srv://abuser:Akhnjofxy5QEoF8P@indexcluster.niofn.mongodb.net/myFirstDatabase?retryWrites=true&w=majority";
const indexClient = new MongoClient(uri2, { useNewUrlParser: true, useUnifiedTopology: true, serverApi: ServerApiVersion.v1 });

const collectionName = 'btabla2';

async function valami(){
    try{
        await client.connect();
        await indexClient.connect();

        let docs = [];
        let docsPk = [];
        let docsUnique = [];
        let docsNonunique = [];
        let docsNonuniqueObject = {};
        
        try{
            await client.db('beszuras').dropCollection(collectionName);
             //await indexClient.db('beszuras').dropDatabase();
        }
        catch(e){}
        

        for(let i = 0; i < 100000; i++){
            let pk = i;
            let unique = (i * 2);
            let nonunique = Math.floor(i * Math.random());
            let nonindex = Math.floor(50000 * Math.random());
            docs.push({_id: pk, values: `${nonunique}#${nonindex}#${unique}#`});
            docsPk.push({_id: pk, "pk": pk});
            docsUnique.push({_id: unique, pk: pk});

            if(docsNonuniqueObject[nonunique] === undefined){
                docsNonuniqueObject[nonunique] = [];
            }
            docsNonuniqueObject[nonunique].push(pk);
        }
        for(k of Object.keys(docsNonuniqueObject)){
            docsNonunique.push({_id: parseInt(k), pks: docsNonuniqueObject[k]});
        }

        await client.db('beszuras').collection(collectionName).insertMany(docs);
        console.log(1);
        await indexClient.db('beszuras').collection(`${collectionName}.pk`).insertMany(docsPk);
        console.log(2);
        await indexClient.db('beszuras').collection(`${collectionName}.unique`).insertMany(docsUnique);
        console.log(3);
        await indexClient.db('beszuras').collection(`${collectionName}.nonunique`).insertMany(docsNonunique);
        console.log(4);
    }
    finally{
        await client.close();
        await indexClient.close();
    }
    
}
valami();