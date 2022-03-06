const net = require('net')
const fs = require('fs');
const { MongoClient, ServerApiVersion } = require('mongodb');
const { fileURLToPath } = require('url');
const { wrap } = require('module');
const uri = "mongodb+srv://abuser:oobS68tBrelJayOp@abprojekt.4qafu.mongodb.net/myFirstDatabase?retryWrites=true&w=majority";

const client = new MongoClient(uri, { useNewUrlParser: true, useUnifiedTopology: true, serverApi: ServerApiVersion.v1 });

function createDatabase(value){
    if (!fs.existsSync(`databases/${value}`)){
        fs.mkdirSync(`databases/${value}`);
        return "OK";
    }
    else{
        return "NOT OK";
    }
}

function getDatabases(){
    return fs.readdirSync("databases/", { withFileTypes: true })
        .filter(dir => dir.isDirectory())
        .map(dir => dir.name)
}

function dropDatabase(value){
    if(fs.existsSync(`databases/${value}`)){
        fs.rmSync(`databases/${value}`,  { recursive: true, force: true });
        return 'OK';
    }
    else{
        return 'NOT OK';
    }
}

function createTable(value){
    let fname = `database/${value.database}/${value.tableName}.json`;
    if(!fs.existsSync("databases/" + value.database) && !fs.existsSync(fname)){
        fs.writeFileSync(fname, JSON.toString(value.attributes));
        return "OK";
    }
    else{
        return "NOT OK";
    }
}


const server = net.createServer((socket) => {

    socket.on('data', (data) =>{
        data = JSON.parse(data.toString())
        console.log("I got a request: " + data.command)
        let answer;
        switch(data.command){
            case "Create Database":
                answer = createDatabase(data.value);
                break;
            case "Get Databases":
                answer = getDatabases();
                break;
            case "Drop Database":
                answer = dropDatabase(data.value);
                break;
            case "Create Table":
                answer = createTable(data.value);
                break;
        }

        socket.write(answer + '\n');
        socket.pipe(socket);
        socket.destroy();
    });

    
})


server.listen(2500, () =>{
    client.connect()
})

server.on('close', () =>{
    client.close();
})

async function hello(){
    let y = getDatabases();
    console.log(y);
}

hello();