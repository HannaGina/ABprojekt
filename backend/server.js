const net = require('net')
const fs = require('fs');
const { MongoClient, ServerApiVersion } = require('mongodb');
const uri = "mongodb+srv://abuser:oobS68tBrelJayOp@abprojekt.4qafu.mongodb.net/myFirstDatabase?retryWrites=true&w=majority";

const client = new MongoClient(uri, { useNewUrlParser: true, useUnifiedTopology: true, serverApi: ServerApiVersion.v1 });

const port = 2500;

function createDatabase(value){
    if (!fs.existsSync(`databases/${value}`)){
        fs.mkdirSync(`databases/${value}`);
        client.db(value).collection("temp").in;          
        return "OK";
    }
    else{
        return "Letezik mar az adatbazis.";
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
        return 'Nem letezik az adatbazis.';
    }
}

function createTable(value){
    let fname = `databases/${value.database}/${value.table}/${value.table}.json`;
    let atrributeNames = value.attributes.map(a => a.name);
    let areAtrributeNamesUnique = atrributeNames.every((e, i) => atrributeNames.indexOf(e) == i);
    if(value.table === ''){
        return "A tablanak kell nevet adni.";
    }
    else if(!areAtrributeNamesUnique){
        return "Tobb ugyanolyan nevu attributum van.";
    }
    else if(atrributeNames.some(e => e == '')){
        return "Van ures mezo.";
    }
    else if(value.attributes.filter(e => e.pk).length != 1){
        return "Kell pontosan egy primary key!";
    }
    else if(fs.existsSync("databases/" + value.database) && !fs.existsSync(fname)){
        value.attributes.map(e => {if(e.pk){e.index = true; e.unique = true;}});
        value.attributes.map(e => {if(e.ftable === '' || e.ftable === null){e.fk = false}});
        
        fs.mkdir(`databases/${value.database}/${value.table}`, (err) =>{
            return "Letezik a tabla";
        });
        value.attributes.forEach(e => {
            if(e.index){
                fs.writeFileSync(`databases/${value.database}/${value.table}/${e.name}.ind`, "");
            } 
        });
        fs.writeFileSync(fname, JSON.stringify(value.attributes));
        return "OK";
    }
    else{
        return "Letezik mar ez a tabla.";
    }
}

function getTables(value){
    return fs.readdirSync(`databases/${value}/`, { withFileTypes: true })
        .map(file => file.name.replace("", ""))
}

function dropTable(value){
    let fname = `databases/${value.database}/${value.table}`;
    if(fs.existsSync(fname)){
        let somethingDependsOnThis = false;
        fs.readdirSync(`databases/${value.database}`).forEach(dir =>{
            const attributes = require(`./databases/${value.database}/${dir}/${dir}.json`)
            if(attributes.some(a => a.fk && (a.ftable === value.table))){
                somethingDependsOnThis = true;
            }
        })
        if(somethingDependsOnThis){
            return "Valami hivatkozik erre a tablara";
        }
        else{
            fs.rmSync(fname,  { recursive: true, force: true });
            return "OK";
        }
    }
    else{
        return "A tabla nem letezik.";
    }
}

function getAttributesByType(value){
    if(value.table === '' || value.table === null){
        return '';
    }
    var attributes = require(`./databases/${value.database}/${value.table}/${value.table}.json`);
    return attributes.filter(a => a.type === value.type).map(a => a.name).toString()
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
            case "Get Tables":
                answer = getTables(data.value);
                break;
            case "Drop Table":
                answer = dropTable(data.value);
                break;
            case "Get Attributes By Type":
                answer = getAttributesByType(data.value);
                break;
        }

        socket.write(answer + '\n');
        socket.pipe(socket);
        socket.destroy();
    });
})


server.listen(port, async () =>{
    console.log(`Server is listening on http://localhost:${port}`)
    await client.connect();
})

server.on('close', () =>{
    client.close();
})