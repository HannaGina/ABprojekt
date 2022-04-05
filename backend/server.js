const net = require('net')
const fs = require('fs');
const { MongoClient, ServerApiVersion } = require('mongodb');
const uri = "mongodb+srv://abuser:CtquLglKDUxGQ0Su@abprojekt.4qafu.mongodb.net/test6?retryWrites=true&w=majority";
const client = new MongoClient(uri, { useNewUrlParser: true, useUnifiedTopology: true, serverApi: ServerApiVersion.v1 });


const uri2 = "mongodb+srv://abuser:Akhnjofxy5QEoF8P@indexcluster.niofn.mongodb.net/myFirstDatabase?retryWrites=true&w=majority";
const indexClient = new MongoClient(uri, { useNewUrlParser: true, useUnifiedTopology: true, serverApi: ServerApiVersion.v1 });


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
        client.db(value).dropDatabase();
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
        value.attributes.map(e => {if(e.pk){e.index = false; e.unique = true;}});
        value.attributes.map(e => {if(e.ftable === '' || e.ftable === null){e.fk = false}});
        
        fs.mkdir(`databases/${value.database}/${value.table}`, (err) =>{
            return "Letezik a tabla";
        });
        fs.writeFileSync(fname, JSON.stringify(value.attributes));
        client.db(value.database).createCollection(value.table);
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
    if(value.table == '' || value.table == null){
        return "Nincs neve a tablanak";
    }
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
            client.db(value.database).dropCollection(value.table);
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
    return attributes.filter(a => a.type === value.type && a.unique).map(a => a.name).toString();
}

function getTableValueNames(value){
    var pathName = `./databases/${value.database}/${value.table}/${value.table}.json`;
    if(fs.existsSync(pathName)){
        var valami ={};
        require(pathName).forEach(v => valami[v.name]= v.type);
        return JSON.stringify(valami);
    }
    else{
        return "Nem letezik a tabla";
    }
}

function getPrimaryKey(value){
    var pathName = `./databases/${value.database}/${value.table}/${value.table}.json`;
    var cells = require(pathName);
    var primary;
    console.log(value);
    cells.forEach(c => {if(c.pk){primary = c.name}})
    return primary;
}

async function insertIntoTable(value) {
    var pathName = `./databases/${value.database}/${value.table}/${value.table}.json`;
    if (fs.existsSync(pathName)) {
        var cellTypes = {};
        require(pathName).forEach(c => cellTypes[c.name] = c.type);
        for (var ckey of Object.keys(value.cells)) {
            if (parameterToType(value.cells[ckey], cellTypes[ckey]) == undefined) {
                return `${ckey} fomatuma nem egyezik meg`;
            }
        }
        var primaryKey = getPrimaryKey(value);
        var otherValues = "";
        Object.keys(value.cells).forEach(ckey => {if(ckey != primaryKey) otherValues += value.cells[ckey] + "#"});
        var toInsert = {_id: value.cells[primaryKey], values: otherValues};
        try{
            await client.db(value.database).collection(value.table).insertOne(toInsert);
        }
        catch(err){
            return "Letezik mar a primary key.";
        }
        return "OK";
    }
    else {
        return "Nem letezik a tabla :(.";
    }
}

function parameterToType(p, type){
    switch(type){
        case 'int':
            if(parseInt(p) == p){
                return parseInt(p);
            }
            else{
                return undefined;
            }
        case 'float':
            if(parseFloat(p) == p){
                return parseInt(p);
            }
            else{
                return undefined;
            }
        case 'string':
            if(p.toString() !== "" && !/#/.test(p)){
                return p.toString();
            }
            else{
                return undefined;
            }
        case 'date':
            if(/^\d{4}\/\d{2}\/\d{2}$/.test(p) && new Date(p) != 'Invalid Date'){
                return p;
            }
            else{
                return undefined;
            }
        case 'datetime':
            if(/^\d{4}\/\d{2}\/\d{2}:\d{2}:\d{2}$/.test(p) && new Date(p) != 'Invalid Date'){
                console.log(1 + " " + new Date(p));
                return p;
            }
            else{
                console.log(/^\d{4}\/\d{2}\/\d{2}$:\d{2}:\d{2}/.test(p));
                return undefined;
            }
        case 'bit':
            if(p === '0'){
                return 0;
            }
            else if(p === '1'){
                return 1;
            }
            else{
                return undefined;
            }
        default:
            return undefined;
    }
}


async function getDocumentsFromTable(value){
    var documents = await client.db(value.database).collection(value.table).find().toArray();
    var array = [];
    documents.forEach(d => {
        array.push([]);
        array[array.length - 1].push(d._id);
        var values = d.values.split('#');
        array[array.length - 1] = array[array.length - 1].concat(values);
    });
    return array;
}

function deleteDocumentsFromTable(value){
    console.log(value);
    value.cells.forEach(id =>{
        client.db(value.database).collection(value.table).deleteOne({_id: id}, (err, obj) =>{
            if(err){
                console.log("Egy bejegyzes nem letezett.:(");
            }
        });
    });
    return "OK";
}

const server = net.createServer((socket) => {

    socket.on('data', async (data) =>{
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
            case "Get Table Values":
                answer = getTableValueNames(data.value);
                break;
            case "Insert Into Table":
                answer = await insertIntoTable(data.value);
                break;
            case "Get Primary Key":
                answer = getPrimaryKey(data.value);
                break;
            case "Get Documents From Table":
                answer = await getDocumentsFromTable(data.value);
                break;
            case "Delete Documents From Table":
                answer = deleteDocumentsFromTable(data.value);
                break;        
        }
        console.log(answer);
        socket.write(answer + '\n');
        socket.pipe(socket);
        socket.destroy();
    });
})


server.listen(port, async () =>{
    console.log(`Server is listening on http://localhost:${port}`)
    client.connect(err =>{
        if(err){
            console.log(err)
        }
    })
    indexClient.connect(err =>{
        console.log("HELLO INDEXCLIENT");
        if(err){
            console.log(err)
        }
    });
})

server.on('close', () =>{
    client.close();
})

